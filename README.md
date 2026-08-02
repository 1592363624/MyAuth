# MyAuth 部署配置说明（CI/CD + Secrets 管理）

> 本文档记录 MyAuth 项目的自动化部署流程、配置文件覆盖逻辑、需要特别注意的事项，
> 以及 GitHub Secrets 的详细配置清单。
>
> 适用代码仓库：MyAuth（后端 `myauth/` + 前端 `myauth-web/`）
> 目标环境：Windows 服务器 + 宝塔面板

---

## 一、核心设计原则

1. **真实生产配置绝不进 Git 仓库**
   - 本地开发配置可能含敏感数据（数据库密码、邮箱授权码、API 密钥等），因此不入库。
   - 仓库只提交 `.example` 模板文件，供参考格式。
   - 真实生产配置通过 **整文件 GitHub Secrets** 在 CI 运行时直接写出到服务器。

2. **配置外置、不硬编码**
   - 所有服务器地址、路径、账号密码、API 地址、密钥等可变项，全部通过 Secrets 注入。
   - 业务代码中不写死任何生产环境相关常量。

3. **服务器现有配置优先、CI 可覆盖**
   - 后端：Spring Boot 优先读取 jar 同目录的 `config/application.yml`（外部配置优先级高于 jar 内）。
   - CI 会通过整文件 Secret 直接写出/覆盖该文件；若未配置对应 Secret，则保留服务器现有文件。

---

## 二、CI 工作流（GitHub Actions）

工作流文件：`.github/workflows/deploy-to-bt-win.yml`

### 触发条件
- 推送到 `master` 分支时自动触发。
- 也可在 Actions 页面手动触发（`workflow_dispatch`），方便首次验证或失败重试。

### 执行步骤概览

| 步骤 | 动作 | 关键说明 |
| --- | --- | --- |
| 1 | 检出代码 | `actions/checkout@v4` |
| 2 | 配置 JDK 17 | 与 `pom.xml` 中 `java.version` 一致 |
| 3 | Maven 构建后端 | `mvn -B package -DskipTests`；防御性删除可能存在的真实 `config/application.yml`，避免误打入 jar |
| 4 | jar 重命名 | 统一命名为 `myauth.jar`，避免版本号变化导致重启脚本失效 |
| 5 | 配置 Node.js 18 | 使用 yarn（锁定 `yarn.lock`） |
| 6 | 前端构建 | 用 `FRONTEND_CONFIG_JSON` 整文件 Secret 写出 `public/serverConfig.json` 后 `yarn build` |
| 7 | SCP 上传后端 jar | 上传到 `BACKEND_REMOTE_DIR` |
| 8 | SCP 上传前端 dist | 直接覆盖同名文件到 `FRONTEND_REMOTE_DIR`（**不清空**目标目录，避免误删其他文件） |
| 9 | SSH 重启后端 | PowerShell 脚本：写配置 → 备份 → 停旧进程 → 启新 jar → 端口探活 → 失败回滚 |

### 部署后会"覆盖"哪些文件（重点）

> ⚠️ 以下内容由 CI 在每次部署时**主动覆盖**，请务必保证 Secrets 中的内容 = 服务器上当前可用的正确配置。

1. **前端 `serverConfig.json`**
   - 覆盖位置：`myauth-web/public/serverConfig.json`（构建产物内）。
   - 数据来源：Secret `FRONTEND_CONFIG_JSON` 的**完整文件内容**。
   - 行为：只要配置了 `FRONTEND_CONFIG_JSON`，就会用其内容**整文件覆盖**写出，再执行 `yarn build`。

2. **后端 `config/application.yml`**
   - 覆盖位置：服务器 `BACKEND_REMOTE_DIR/config/application.yml`，例如 `C:\wwwroot\myauth\config\application.yml`。
   - 数据来源：Secret `BACKEND_CONFIG_YML` 的**完整文件内容**。
   - 行为：只要配置了 `BACKEND_CONFIG_YML`，就会用其内容**整文件覆盖**写出（UTF-8 BOM 编码，Spring 读取必需）。
   - 该路径是 Spring Boot 标准外部 config 子目录，优先级高于 jar 内部配置，且后续重传 jar **不会**覆盖它。

3. **后端 jar 文件**
   - 覆盖位置：服务器 `BACKEND_REMOTE_DIR/myauth.jar`。
   - 行为：SCP `overwrite: true` 直接覆盖；部署前会自动备份为 `myauth.jar.bak`，供失败回滚。

4. **前端静态文件（dist 全部）**
   - 覆盖位置：服务器 `FRONTEND_REMOTE_DIR/` 下的同名文件。
   - 行为：SCP `overwrite: true` 直接覆盖同名文件，**不清空**目标目录，避免误删目标目录下其他文件（如手动放置的静态资源）；不再生成的前端旧文件残留不影响运行（Nginx 不会主动引用已不存在的入口）。

---

## 三、后端重启脚本的健壮性保障

SSH 步骤（步骤 9）的 PowerShell 脚本保证"更新后项目不会雪崩"：

1. 启动新 jar 前，先备份旧 jar 为 `myauth.jar.bak`。
2. 用 WMI 精准匹配命令行包含 `myauth.jar` 的 java 进程并停止。
3. 启动后轮询 6401 端口（最多约 90 秒）确认存活。
4. 若新 jar 启动失败（端口不通），自动回滚到 `myauth.jar.bak` 并重启旧版本。
5. 兜底：新旧都起不来时抛出错误，由 Actions 标记失败便于人工介入，但此时旧版本已在运行。

> 监听端口固定为 **6401**，对应宝塔 Java 项目执行命令中的 `--server.port=6401`。
> 如需修改，请同时修改工作流脚本中的 `$listenPort` 与宝塔面板中的执行命令。

---

## 四、生产环境关键路径（请核对与你服务器一致）

| 项目 | 路径/值 | 来源 |
| --- | --- | --- |
| 后端 jar 目录 | `C:/wwwroot/myauth` | Secret `BACKEND_REMOTE_DIR` |
| 运行 jar 名 | `myauth.jar` | 工作流 `env.BACKEND_JAR_NAME` |
| 后端外部配置 | `C:/wwwroot/myauth/config/application.yml` | Secret `BACKEND_CONFIG_YML` 写出 |
| 前端网站根目录 | `C:/wwwroot/my.52shell.ltd` | Secret `FRONTEND_REMOTE_DIR` |
| 后端监听端口 | `6401` | 宝塔执行命令 `--server.port=6401` |
| Nginx 反向代理 | `/myauth/` → `http://127.0.0.1:6401/myauth/` | 服务器 Nginx 配置 |

---

## 五、GitHub Secrets 配置清单

> 配置入口：仓库 `Settings → Secrets and variables → Actions → New repository secret`
> 单条 Secret 大小上限为 **64 KB**，整文件配置方案完全够用。

### 必填 Secrets（共 8 个）

| Secret 名称 | 类型 | 内容 / 用途 | 写入位置 |
| --- | --- | --- | --- |
| `BACKEND_CONFIG_YML` | 整文件 | 后端生产 `application.yml` **完整内容**（含 DB、Redis、邮件、genKey 等）。部署时**覆盖**写出到服务器 `config/application.yml` | 服务器外部配置 |
| `FRONTEND_CONFIG_JSON` | 整文件 | 前端生产 `serverConfig.json` **完整内容**（含 API 根地址、备案号等）。部署时**覆盖**写出后再 build | 前端构建产物 |
| `BACKEND_REMOTE_DIR` | 路径 | 后端 jar 在服务器上的目录，如 `C:/wwwroot/myauth/` | SCP 目标 / 脚本变量 |
| `FRONTEND_REMOTE_DIR` | 路径 | 前端网站根目录，如 `C:/wwwroot/my.52shell.ltd/` | SCP 目标 |
| `WIN_HOST` | 连接 | 服务器 SSH 主机名或 IP，如 `52shell.ltd` | SSH/SCP |
| `WIN_PORT` | 连接 | SSH 端口，默认 `22` | SSH/SCP |
| `WIN_USER` | 连接 | SSH 登录用户名，如 `Administrator` | SSH/SCP |
| `WIN_SSH_KEY` | 密钥 | 本地私钥内容（无口令 ed25519 等），用于密钥登录 | SSH/SCP |

> ⚠️ 历史方案中曾使用 `FRONTEND_API_URL` 等拆散变量，当前工作流已弃用，**请勿再配置该 Secret**，避免混淆。

### Secrets 填写注意事项

1. **整文件类 Secret（`BACKEND_CONFIG_YML` / `FRONTEND_CONFIG_JSON`）**
   - 直接粘贴文件的**全部内容**，不要加引号、不要转义。
   - YAML 中若含特殊字符（如 `【】`、冒号、井号），建议给对应值加双引号，例如 `nickname: "【小爱同学】网络授权中心"`，避免 YAML 解析歧义。
   - 该内容是"始终覆盖"策略：**修改生产配置 = 更新对应 Secret**，而不是改服务器文件（改了也会被下次部署覆盖）。

2. **YAML 缩进**：`BACKEND_CONFIG_YML` 必须保持正确的 YAML 缩进（2 空格），否则 Spring 启动会失败。

3. **路径分隔符**：Windows 路径在 Secret 中用正斜杠或反斜杠均可（如 `C:/wwwroot/myauth/`），SCP 兼容。

4. **SSH 私钥权限**：服务器 Windows OpenSSH 对 `Administrator` 用户从
   `C:\ProgramData\ssh\administrators_authorized_keys` 读取公钥，文件权限须仅 `Administrators` / `SYSTEM`。

---

## 六、需要特别注意的事项（Checklist）

- [ ] **Secret 内容必须等于服务器当前可用配置**。因为 CI 是"始终覆盖"，若 Secret 内容过期（如改了数据库密码却没更新 Secret），部署后配置会回退到旧 Secret 值。
- [ ] **邮箱授权码类敏感信息只存在于 Secret**，不要写进仓库任何文件（包括模板）。
- [ ] 修改 `BACKEND_CONFIG_YML` 后，建议本地用 `yaml` 校验工具确认格式合法，避免 CI 写出非法 YAML 导致 Spring 启动失败、触发回滚。
- [ ] 监听端口 `6401` 与宝塔执行命令必须保持一致，改一处要同步改另一处。
- [ ] 若仓库为**公开仓库**，历史提交中可能残留过真实配置文件，需用 `git filter-repo` 或 BFG 重写历史清理。
- [ ] 模板文件 `config/application.example.yml`、`serverConfig.example.json` 仅作格式参考，不含真实值，可安全入库。
- [ ] 首次部署前，确保服务器上 `BACKEND_REMOTE_DIR` 与 `FRONTEND_REMOTE_DIR` 目录已创建且具备写入权限。
- [ ] 部署失败不要慌：脚本会自动回滚到 `myauth.jar.bak`，旧版本仍在运行；查看 Actions 日志定位问题后重试。

---

## 七、本地开发与生产的关系

- 本地开发：`myauth-web` 的 `main.js` 在开发环境使用 `.env` 的本地地址，生产环境才读取 `serverConfig.json`。
- 本地真实配置文件（`config/application.yml`、`serverConfig.json`）已被 `.gitignore` 忽略，**不会入库、不会泄露**。
- 仓库中仅保留 `.example` 模板，供团队成员了解配置格式。
- 生产配置的唯一权威来源 = GitHub Secrets，服务器上的 `config/application.yml` 只是 Secrets 的"落地副本"，每次部署刷新。


### 自用改  https://www.myauthx.cn/  的项目

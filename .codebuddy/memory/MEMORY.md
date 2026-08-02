# MyAuth 项目长期记忆

## 项目概况

- 项目路径：`d:/WorkSpace/IntelliJIDEAWorkspace/MyAuth`
- 后端：`myauth/`，Spring Boot 2.6.2 + Maven + Java 17，端口 8081，context-path `/myauth`
- 前端：`myauth-web/`，Vue 2 + Ant Design Vue + vue-cli + Yarn

## 部署偏好

- 目标环境：Windows 服务器 + 宝塔面板
- 自动化部署：GitHub Actions（工作流文件 `.github/workflows/deploy-to-bt-win.yml`）
- 后端交付物：Spring Boot 可执行 jar，上传后通过 PowerShell 脚本重启 Java 进程
- 前端交付物：`dist/` 静态文件，上传到宝塔网站根目录
- 配置外置：服务器地址、路径、API 地址、账号密码等全部通过 GitHub Secrets 注入

## 生产环境路径

- 后端 jar 存放目录：`C:/wwwroot/myauth`
- 后端运行 jar 名称：`myauth.jar`
- 后端外部配置文件：`C:/wwwroot/myauth/config/application.yml`（Spring Boot 标准 config 子目录约定，文件名不带 -prod 后缀，作为主配置直接用，不依赖 profile；重传 jar 不覆盖）
- 前端网站根目录：`C:/wwwroot/my.52shell.ltd`

## 宝塔 Java 项目配置

- 面板中已创建 Java 项目：`myauth`
- 项目类型：`SpringBoot`
- 实际运行端口：`6401`（通过 `--server.port=6401` 覆盖 application-prod.yml 的 8081）
- 项目执行命令：`C:\BtSoft\jdk\jdk17\bin\java.exe -jar -Xmx1024M -Xms256M C:\wwwroot\myauth\myauth.jar --server.port=6401`
- Nginx 已配置反向代理：`/myauth/` → `http://127.0.0.1:6401/myauth/`
- **重启方式（2026-08-01 定）：不使用宝塔 API，改用 GitHub Actions SSH 脚本杀进程+启动 jar（进程模式），脚本自带备份/回滚，保证更新失败也能恢复旧版本不雪崩**

## SSH 部署密钥

- 本地私钥：`C:\Users\15923\.ssh\id_ed25519`（无口令 ed25519，注释 github-actions），已验证可登录 `Administrator@52shell.ltd`
- Windows OpenSSH 对 Administrator 用户从 `C:\ProgramData\ssh\administrators_authorized_keys` 读公钥，权限须仅 Administrators/SYSTEM

## 约定

- 所有代码注释使用中文
- 可变化配置项必须外置，不硬编码在业务代码中
- 生产环境 API 地址通过 `VUE_APP_PublishRootUrl` 环境变量覆盖 `.env` 默认值

## 敏感配置外置决策（2026-08-02 定）

- **核心原则**：真实生产配置绝不进 Git 仓库；只提交 `.example` 模板 + 用 Secrets 注入。
- 已执行（最终方案：模板入库 + 本地真实配置不入库）：
  - 仓库提交模板：`config/application.example.yml`（含 `${MYAUTH_*}` 占位符 + 本地默认值）、`serverConfig.example.json`。
  - 本地真实文件 `config/application.yml`、`serverConfig.json` 被 `.gitignore` 忽略、不入库（用户顾虑本地开发配置可能含敏感数据）。
  - 前端 `main.js` 已改为：开发环境用 `.env` 的本地地址，生产环境才读 `serverConfig.json`。
  - CI 工作流：前端 build 用 `FRONTEND_API_URL` Secret 覆盖 `serverConfig.json`；后端仅做防御性删除真实 `config/application.yml`（仓库只有模板，jar 内为占位符默认值）。
  - 生产配置由服务器外部 `C:/wwwroot/myauth/config/application.yml` 提供（Spring Boot 优先读取，重传 jar 不覆盖）；SSH 脚本兜底：外部文件存在则跳过。
- 待办：GitHub 远程历史中仍含旧真实文件，需 `git filter-repo`/BFG 重写历史清理（若仓库公开则必须做）。

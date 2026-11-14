package cn.daenx.myauth.main.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import cn.daenx.myauth.base.annotation.*;
import cn.daenx.myauth.main.entity.Storage;
import cn.daenx.myauth.main.service.*;
import cn.daenx.myauth.util.CheckUtils;
import cn.daenx.myauth.util.FeatureSignUtil;
import cn.daenx.myauth.util.IpUtil;
import cn.daenx.myauth.util.MyUtils;
import cn.daenx.myauth.base.annotation.*;
import cn.daenx.myauth.base.vo.Result;
import cn.daenx.myauth.main.entity.Soft;
import cn.daenx.myauth.main.entity.User;
import cn.daenx.myauth.main.entity.Version;
import cn.daenx.myauth.main.enums.SoftEnums;
import cn.daenx.myauth.main.service.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 软件使用API接口
 *
 * @author DaenMax
 */
@Slf4j
@RestController
@Tag(name = "软件SDK")
@RequestMapping("/soft")
public class SoftApiController {
    @Resource
    private IVersionService versionService;
    @Resource
    private IUserService userService;
    @Resource
    private IJsService jsService;
    @Resource
    private IDataService dataService;
    @Resource
    private IEventService eventService;
    @Resource
    private IStorageService storageService;
    @Resource
    private FeatureSignKeyService featureSignKeyService;

    /**
     * 初始化软件
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("/init")
    public Result init(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        JSONObject retJson = new JSONObject(true);
        retJson.put("Name", soft.getName());
        retJson.put("Status", soft.getStatus());
        retJson.put("Type", soft.getType());
        retJson.put("BindDeviceCode", soft.getBindDeviceCode());
        retJson.put("HeartTime", soft.getHeartTime());
        return Result.ok("初始化成功", retJson);
    }

    /**
     * 检测更新
     *
     * @param request
     * @return
     */
    @SoftValidated
    @DataDecrypt
    @SignValidated
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("/checkUpdate")
    public Result checkUpdate(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        String vkey = jsonObject.getString("vkey");
        if (CheckUtils.isObjectEmpty(vkey)) {
            return Result.error("缺少vkey参数");
        }
        Version version = new Version();
        version.setVkey(vkey);
        version.setFromSoftId(soft.getId());
        return versionService.checkUpdate(version, soft);
    }

    /**
     * 获取最新的一个版本
     *
     * @param request
     * @return
     */
    @SoftValidated
    @DataDecrypt
    @SignValidated
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("/getNewVersion")
    public Result getNewVersion(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        String vkey = jsonObject.getString("vkey");
        if (CheckUtils.isObjectEmpty(vkey)) {
            return Result.error("缺少vkey参数");
        }
        return versionService.getNewVersion(soft);
    }

    /**
     * 注册
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("/register")
    public Result register(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        User user = jsonObject.getJSONObject("data").toJavaObject(User.class);
        if (CheckUtils.isObjectEmpty(user)) {
            return Result.error("参数错误");
        }
        if (soft.getRegister().equals(SoftEnums.REGISTER_DISABLE.getCode())) {
            return Result.error("当前不允许注册新用户");
        }
        if (CheckUtils.isObjectEmpty(user.getUser())) {
            return Result.error("账号不能为空");
        }
        String ip = IpUtil.getIpAddr(request);
        user.setLastIp(ip);
        return userService.register(user, soft);
    }

    /**
     * 登录
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("login")
    public Result login(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        Version version = (Version) request.getAttribute("obj_version");
        User user = jsonObject.getJSONObject("data").toJavaObject(User.class);
        String ip = IpUtil.getIpAddr(request);
        user.setLastIp(ip);
        user.setLastTime(Integer.valueOf(MyUtils.getTimeStamp()));
        user.setFromVerId(version.getId());
        user.setFromVerKey(version.getVkey());
        return userService.login(user, soft);
    }

    /**
     * 心跳
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @UserLogin
    @BanValidated(is_ip = true, is_device_code = true, is_user = true)
    @PostMapping("heart")
    public Result heart(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        User user = (User) request.getAttribute("obj_user");
        String token = jsonObject.getJSONObject("data").getString("token");
        user.setLastTime(Integer.valueOf(MyUtils.getTimeStamp()));
        return userService.heart(user, soft, token);
    }

    /**
     * 离线
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @UserLogin
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("offLine")
    public Result offLine(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        User user = (User) request.getAttribute("obj_user");
        String token = jsonObject.getJSONObject("data").getString("token");
        user.setLastTime(Integer.valueOf(MyUtils.getTimeStamp()));
        return userService.offLine(user, soft, token);
    }

    /**
     * 使用卡密
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("/useCkey")
    public Result useCkey(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        User user = jsonObject.getJSONObject("data").toJavaObject(User.class);
        if (CheckUtils.isObjectEmpty(user)) {
            return Result.error("参数错误");
        }
        if (CheckUtils.isObjectEmpty(user.getUser())) {
            return Result.error("账号不能为空");
        }
        if (CheckUtils.isObjectEmpty(user.getCkey())) {
            return Result.error("卡密不能为空");
        }
        return userService.useCkey(user, soft);
    }

    /**
     * 获取回复
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("/getMsg")
    public Result getMsg(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        Version version = (Version) request.getAttribute("obj_version");
        String keyword = jsonObject.getJSONObject("data").getString("keyword");
        String ver = jsonObject.getJSONObject("data").getString("ver");
        if (CheckUtils.isObjectEmpty(keyword)) {
            return Result.error("keyword不能为空");
        }
        return userService.getMsg(soft, version, keyword, ver);
    }

    /**
     * 签发功能票据（feature token）
     *
     * 与登录/心跳接口的区别：point 不取自请求、也不取自 Redis 缓存快照，
     * 而是由服务端从 ma_user 表实时读取，并由服务端按 feature_rules 直接算出
     * features 布尔表后用 Ed25519 私钥签名。客户端只内嵌公钥验签，
     * 因此伪造登录响应、篡改远程配置、搭假验证服务器都无法解锁功能。
     *
     * 返回 result：
     *   payload - 待签名原文（v1|kid|skey|user|point|exp|nonce|deviceCode|features|disabledModules）
     *   sig     - Ed25519 签名 Base64
     *   exp     - 过期时间（Unix 秒）
     *   kid     - 密钥 ID
     *   features- 冗余字段，仅便于调试；客户端以 payload 内已签名的内容为准
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @UserLogin
    @BanValidated(is_ip = true, is_device_code = true, is_user = true)
    @PostMapping("/featureToken")
    public Result featureToken(HttpServletRequest request) {
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        User user = (User) request.getAttribute("obj_user");
        String nonce = jsonObject.getJSONObject("data").getString("nonce");
        if (CheckUtils.isObjectEmpty(nonce)) {
            return Result.error("nonce不能为空");
        }

        // autoCreate=true：软件首次申请票据时若还没配密钥，服务端自动生成一把，
        // 避免接入方忘记去后台点「生成新密钥」导致客户端静默回退到旧点数逻辑。
        FeatureSignKeyService.SignKeyRecord key = featureSignKeyService.getActiveKey(soft.getId(), true);
        if (key == null) {
            log.warn("软件 {} 的功能签名密钥自动生成失败，拒绝签发功能票据", soft.getSkey());
            return Result.error("该软件尚未配置功能签名密钥，且自动生成失败");
        }

        // 实时点数：不用 obj_user.point（Redis 快照可能滞后于后台扣分/充值）
        int point = featureSignKeyService.getRealtimePoint(user.getId());
        List<String> disabledModules = new ArrayList<>();
        Map<String, Boolean> features = FeatureSignUtil.evaluateFeatures(
                key.featureRules, user.getUser(), point, disabledModules);

        // 功能项由每个软件自己在后台声明。一个功能项都没声明时不能签发票据：
        // 客户端遇到票据里没有的功能标识会按「未知功能」放行，
        // 那样等于所有锁都是开着的，比直接报错更危险。
        if (features.isEmpty()) {
            log.warn("软件 {} 未声明任何功能项，拒绝签发功能票据", soft.getSkey());
            return Result.error("该软件尚未配置功能项，请在后台「签名密钥 → 编辑规则」中添加");
        }

        long exp = Long.parseLong(MyUtils.getTimeStamp()) + 300L; // 票据 5 分钟有效
        String payload = FeatureSignUtil.buildPayload(
                key.kid, soft.getSkey(), user.getUser(), point, exp, nonce,
                user.getDeviceCode(), features, disabledModules);

        String sig;
        try {
            sig = FeatureSignUtil.sign(payload, key.privateKey);
        } catch (Exception e) {
            log.error("功能票据签名失败: soft={}, kid={}", soft.getSkey(), key.kid, e);
            return Result.error("功能票据签名失败");
        }

        JSONObject retJson = new JSONObject(true);
        retJson.put("payload", payload);
        retJson.put("sig", sig);
        retJson.put("exp", exp);
        retJson.put("kid", key.kid);
        retJson.put("features", features);
        // 解锁点数只用于客户端展示「还需 N 点」，不进签名 payload
        retJson.put("requiredPoints", FeatureSignUtil.evaluateRequiredPoints(key.featureRules));
        return Result.ok("功能票据签发成功", retJson);
    }

    /**
     * 执行JS
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @UserLogin
    @BanValidated(is_ip = true, is_device_code = true, is_user = true)
    @PostMapping("/runJs")
    public Result runJs(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        String func = jsonObject.getJSONObject("data").getString("func");
        if (CheckUtils.isObjectEmpty(func)) {
            return Result.error("func不能为空");
        }
        String c1 = jsonObject.getJSONObject("data").getString("c1");
        String c2 = jsonObject.getJSONObject("data").getString("c2");
        String c3 = jsonObject.getJSONObject("data").getString("c3");
        String c4 = jsonObject.getJSONObject("data").getString("c4");
        String c5 = jsonObject.getJSONObject("data").getString("c5");
        String c6 = jsonObject.getJSONObject("data").getString("c6");
        String c7 = jsonObject.getJSONObject("data").getString("c7");
        String c8 = jsonObject.getJSONObject("data").getString("c8");
        String c9 = jsonObject.getJSONObject("data").getString("c9");
        String c10 = jsonObject.getJSONObject("data").getString("c10");
        return jsService.runJs(soft, func, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10);
    }

    /**
     * 上报数据
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("/upData")
    public Result upData(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        Version version = (Version) request.getAttribute("obj_version");
        String type = jsonObject.getJSONObject("data").getString("type");
        String content = jsonObject.getJSONObject("data").getString("content");
        String device_info = jsonObject.getJSONObject("data").getString("device_info");
        String device_code = jsonObject.getJSONObject("data").getString("device_code");
        String ip = IpUtil.getIpAddr(request);
        if (CheckUtils.isObjectEmpty(type)) {
            return Result.error("type不能为空");
        }
        if (CheckUtils.isObjectEmpty(content)) {
            return Result.error("content不能为空");
        }
        if (CheckUtils.isObjectEmpty(device_info)) {
            return Result.error("device_info不能为空");
        }
        if (CheckUtils.isObjectEmpty(device_code)) {
            return Result.error("device_code不能为空");
        }
        return dataService.upData(type, content, ip, device_info, device_code, soft, version);
    }

    /**
     * 触发事件
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @UserLogin
    @BanValidated(is_ip = true, is_device_code = true, is_user = true)
    @PostMapping("/letEvent")
    public Result letEvent(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        User user = (User) request.getAttribute("obj_user");
        String name = jsonObject.getJSONObject("data").getString("name");
        String token = jsonObject.getJSONObject("data").getString("token");
        if (CheckUtils.isObjectEmpty(name)) {
            return Result.error("事件name不能为空");
        }
        return eventService.letEvent(name, user, soft, token);
    }

    /**
     * 解绑
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @UserLogin
    @BanValidated(is_ip = true, is_device_code = true, is_user = true)
    @PostMapping("unbind")
    public Result unbind(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        User user = (User) request.getAttribute("obj_user");
        user.setDeviceInfo("");
        user.setDeviceCode("");
        user.setToken("");
        return userService.unbind(user, soft);
    }

    /**
     * 修改密码
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("editPass")
    public Result editPass(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        String user = jsonObject.getJSONObject("data").getString("user");
        String nowPass = jsonObject.getJSONObject("data").getString("nowPass");
        String newPass = jsonObject.getJSONObject("data").getString("newPass");
        if (CheckUtils.isObjectEmpty(user) || CheckUtils.isObjectEmpty(nowPass) || CheckUtils.isObjectEmpty(newPass)) {
            return Result.error("账号、旧密码、新密码不能为空");
        }
        if (nowPass.equals(newPass)) {
            return Result.error("旧密码和新密码不能相同");
        }
        return userService.editPass(user, nowPass, newPass, soft);
    }

    /**
     * 修改资料：昵称和QQ
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @UserLogin
    @BanValidated(is_ip = true, is_device_code = true, is_user = true)
    @PostMapping("editInfo")
    public Result editInfo(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        User user = (User) request.getAttribute("obj_user");
        String newName = jsonObject.getJSONObject("data").getString("newName");
        String newQq = jsonObject.getJSONObject("data").getString("newQq");
        String token = jsonObject.getJSONObject("data").getString("token");
        if (CheckUtils.isObjectEmpty(newName) && CheckUtils.isObjectEmpty(newQq)) {
            return Result.error("新昵称和新QQ不能都为空");
        }
        if (!CheckUtils.isObjectEmpty(user)) {
            user.setName(newName);
        }
        if (!CheckUtils.isObjectEmpty(newQq)) {
            user.setQq(newQq);
        }
        return userService.editInfo(user, soft, token);
    }

    /**
     * 检查账号状态
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("checkUser")
    public Result checkUser(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        Version version = (Version) request.getAttribute("obj_version");
        User user = jsonObject.getJSONObject("data").toJavaObject(User.class);
        String ip = IpUtil.getIpAddr(request);
        user.setLastIp(ip);
        user.setLastTime(Integer.valueOf(MyUtils.getTimeStamp()));
        user.setFromVerId(version.getId());
        user.setFromVerKey(version.getVkey());
        return userService.checkUser(user, soft);
    }

    /**
     * 上报额外存储
     *
     * @param request
     * @return
     */
    @SoftValidated
    @VersionValidated
    @DataDecrypt
    @SignValidated
    @BanValidated(is_ip = true, is_device_code = true, is_user = false)
    @PostMapping("upStorage")
    public Result upStorage(HttpServletRequest request) {
        //不管有没有加密和解密，取提交的JSON都要通过下面这行去取
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = (Soft) request.getAttribute("obj_soft");
        Storage storage = jsonObject.getJSONObject("data").toJavaObject(Storage.class);
        String skey = soft.getSkey();
        String type = jsonObject.getJSONObject("data").getString("type");
        if (CheckUtils.isObjectEmpty(type)) {
            return Result.error("type不能为空");
        }
        if (CheckUtils.isObjectEmpty(storage)) {
            return Result.error("参数错误");
        }
        if (CheckUtils.isObjectEmpty(storage.getContent())) {
            return Result.error("参数不全");
        }
        storage = storageService.toStorage(storage,type,skey);
        if(CheckUtils.isObjectEmpty(storage)){
            return Result.error("参数错误");
        }
        storage.setNumber(null);
        return storageService.addStorage(storage);
    }

}

package cn.daenx.myauth.main.controller.web;

import io.swagger.v3.oas.annotations.tags.Tag;

import cn.daenx.myauth.main.entity.Admin;
import cn.daenx.myauth.main.entity.Role;
import cn.daenx.myauth.util.CheckUtils;
import cn.daenx.myauth.base.annotation.AdminLogin;
import cn.daenx.myauth.base.annotation.NoEncryptNoSign;
import cn.daenx.myauth.base.vo.Result;
import cn.daenx.myauth.base.vo.MyPage;
import cn.daenx.myauth.main.entity.Soft;
import cn.daenx.myauth.main.service.FeatureSignKeyService;
import cn.daenx.myauth.main.service.ISoftService;
import cn.daenx.myauth.util.RedisUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 前端web使用的API接口
 *
 * @author DaenMax
 */
@Slf4j
@RestController
@Tag(name = "软件管理")
@RequestMapping("/web")
public class SoftController {
    @Resource
    private ISoftService softService;
    @Resource
    private FeatureSignKeyService featureSignKeyService;
    @Resource
    private RedisUtil redisUtil;

    /**
     * 获取软件列表
     *
     * @param request
     * @return
     */
    @NoEncryptNoSign
    @AdminLogin
    @PostMapping("getSoftList")
    public Result getSoftList(HttpServletRequest request) {
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = jsonObject.toJavaObject(Soft.class);
        MyPage myPage = jsonObject.toJavaObject(MyPage.class);
        if (CheckUtils.isObjectEmpty(myPage.getPageIndex()) || CheckUtils.isObjectEmpty(myPage.getPageSize())) {
            return Result.error("页码和尺寸参数不能为空");
        }
        return softService.getSoftList(soft, myPage);
    }

    /**
     * 添加软件
     *
     * @param request
     * @return
     */
    @NoEncryptNoSign
    @AdminLogin
    @PostMapping("addSoft")
    public Result addSoft(HttpServletRequest request) {
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = jsonObject.toJavaObject(Soft.class);
        if (CheckUtils.isObjectEmpty(soft)) {
            return Result.error("参数错误");
        }
        if (CheckUtils.isObjectEmpty(soft.getName()) || CheckUtils.isObjectEmpty(soft.getStatus()) || CheckUtils.isObjectEmpty(soft.getType())
                || CheckUtils.isObjectEmpty(soft.getGenKey()) || CheckUtils.isObjectEmpty(soft.getGenStatus()) || CheckUtils.isObjectEmpty(soft.getBindDeviceCode())
                || CheckUtils.isObjectEmpty(soft.getHeartTime()) || CheckUtils.isObjectEmpty(soft.getRegister())
                || CheckUtils.isObjectEmpty(soft.getSignTime())) {
            return Result.error("参数不全");
        }
        return softService.addSoft(soft);
    }

    /**
     * 修改软件
     *
     * @param request
     * @return
     */
    @NoEncryptNoSign
    @AdminLogin
    @PostMapping("updSoft")
    public Result updSoft(HttpServletRequest request) {
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = jsonObject.toJavaObject(Soft.class);
        if (CheckUtils.isObjectEmpty(soft)) {
            return Result.error("参数错误");
        }
        soft.setSkey(null);
        soft.setAddTime(null);
        if (CheckUtils.isObjectEmpty(soft.getId())) {
            return Result.error("id不能为空");
        }
        if (CheckUtils.isObjectEmpty(soft.getName()) && CheckUtils.isObjectEmpty(soft.getStatus())
                && CheckUtils.isObjectEmpty(soft.getType()) && CheckUtils.isObjectEmpty(soft.getGenKey()) && CheckUtils.isObjectEmpty(soft.getGenStatus())
                && CheckUtils.isObjectEmpty(soft.getBindDeviceCode()) && CheckUtils.isObjectEmpty(soft.getHeartTime())
                && CheckUtils.isObjectEmpty(soft.getRegister()) && CheckUtils.isObjectEmpty(soft.getSignTime())) {
            return Result.error("参数不能全部为空");
        }
        return softService.updSoft(soft);
    }

    /**
     * 删除软件，会同步删除版本、卡密、用户、事件、数据、封禁、JS、回复、日志
     *
     * @param request
     * @return
     */
    @NoEncryptNoSign
    @AdminLogin
    @PostMapping("delSoft")
    public Result delSoft(HttpServletRequest request) {
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = jsonObject.toJavaObject(Soft.class);
        if (CheckUtils.isObjectEmpty(soft)) {
            return Result.error("参数错误");
        }
        if (CheckUtils.isObjectEmpty(soft.getId())) {
            return Result.error("id不能为空");
        }
        return softService.delSoft(soft);
    }

    /**
     * 查询软件，根据id或者skey
     *
     * @param request
     * @return
     */
    @NoEncryptNoSign
    @AdminLogin
    @PostMapping("getSoft")
    public Result getSoft(HttpServletRequest request) {
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = jsonObject.toJavaObject(Soft.class);
        if (CheckUtils.isObjectEmpty(soft)) {
            return Result.error("参数错误");
        }
        if (CheckUtils.isObjectEmpty(soft.getId()) && CheckUtils.isObjectEmpty(soft.getSkey())) {
            return Result.error("id和skey不能全部为空");
        }
        return softService.getSoft(soft);
    }

    /**
     * 获取软件列表_全部_简要
     *
     * @param request
     * @return
     */
    @NoEncryptNoSign
    @AdminLogin
    @PostMapping("getSoftListEx")
    public Result getSoftListEx(HttpServletRequest request) {
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Soft soft = jsonObject.toJavaObject(Soft.class);
        return softService.getSoftListEx(soft);
    }

    @NoEncryptNoSign
    @AdminLogin(is_super_role = false)
    @PostMapping("getMySoftListEx")
    public Result getMySoftListEx(HttpServletRequest request){
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Admin admin = (Admin) request.getAttribute("obj_admin");
        Role role = (Role) redisUtil.get("role:" + admin.getRole());
        if (role.getFromSoftId().equals("0")) {
            return Result.error("超级管理员无法使用此接口");
        }
        return softService.getMySoftListEx(admin);
    }

    /**
     * 为软件生成一对功能票据签名密钥。
     * 私钥写入 ma_soft_sign_key 后不再回显；返回值只含 kid 与 publicKey，
     * 公钥由开发者复制进客户端 hardcoded.go 后重新编译。
     */
    @NoEncryptNoSign
    @AdminLogin
    @PostMapping("genFeatureSignKey")
    public Result genFeatureSignKey(HttpServletRequest request) {
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Integer id = jsonObject.getInteger("id");
        if (CheckUtils.isObjectEmpty(id)) {
            return Result.error("软件ID不能为空");
        }
        try {
            return Result.ok("密钥已生成，请复制公钥到客户端", featureSignKeyService.generateAndSave(id));
        } catch (Exception e) {
            log.error("生成功能签名密钥失败: softId={}", id, e);
            return Result.error("生成失败: " + e.getMessage());
        }
    }

    /** 列出某软件的公钥（不含私钥），用于后台核对与轮换 */
    @NoEncryptNoSign
    @AdminLogin
    @PostMapping("listFeatureSignKeys")
    public Result listFeatureSignKeys(HttpServletRequest request) {
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Integer id = jsonObject.getInteger("id");
        if (CheckUtils.isObjectEmpty(id)) {
            return Result.error("软件ID不能为空");
        }
        return Result.ok("获取成功", featureSignKeyService.listPublicKeys(id));
    }

    /** 维护该软件的功能规则 JSON（服务端据此计算 features） */
    @NoEncryptNoSign
    @AdminLogin
    @PostMapping("updFeatureRules")
    public Result updFeatureRules(HttpServletRequest request) {
        JSONObject jsonObject = (JSONObject) request.getAttribute("json");
        Long keyId = jsonObject.getLong("keyId");
        Integer softId = jsonObject.getInteger("softId");
        String rules = jsonObject.getString("rules");
        if (CheckUtils.isObjectEmpty(keyId) || CheckUtils.isObjectEmpty(softId)
                || CheckUtils.isObjectEmpty(rules)) {
            return Result.error("keyId / softId / rules 不能为空");
        }
        int rows = featureSignKeyService.updateRules(keyId, softId, rules);
        if (rows == 0) {
            return Result.error("密钥记录不存在");
        }
        return Result.ok("规则已更新");
    }
}

package com.dp.common.control;

import com.dp.common.constant.ApplicationConstant;
import com.dp.common.exception.BaseException;
import com.dp.common.result.Result;
import com.dp.common.result.ResultCodeEnum;
import com.dp.common.result.Results;
import com.dp.common.session.SessionManager;
import com.dp.common.vo.PortalUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.StringJoiner;

@ControllerAdvice
@Slf4j
public class BaseControl {

  @Autowired
  protected ApplicationContext applicationContext;

  /**
   * 获取当前登录用户token
   */
  public String getToken() {
    RequestAttributes requestAttributes = null;
    try{
      requestAttributes = RequestContextHolder.currentRequestAttributes();
    }catch (IllegalStateException e){
      return "";
    }
    HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
    return StringUtils.defaultIfBlank(request.getParameter(ApplicationConstant.PORTAL_TOKEN_NAME), "");
  }

  /**
   * 获取当前登录用户Uuid
   */
  public String getUserUuid() {
    PortalUserInfo portalUser = this.getPortalUserInfo();
    if (null != portalUser) {
      return portalUser.getUuid();
    }
    return null;
  }

  /**
   * 判断当前用户是否是administrator账号
   */
  public boolean isAdministrator() {
    PortalUserInfo portalUser = this.getPortalUserInfo();
    if (null != portalUser) {
      if (ApplicationConstant.PORTAL_ADMIN_UUID.equals(portalUser.getUuid())) {
        return true;
      }
      return false;
    }
    return false;
  }

  /**
   * 获取当前登录用户名
   */
  public String getUserName() {
    PortalUserInfo portalUser = this.getPortalUserInfo();
    if (null != portalUser) {
      return portalUser.getUserName();
    }
    return  null;
  }

  /**
   * 全局异常捕捉处理
   */
  @ResponseBody
  @ExceptionHandler(value = Exception.class)
  public Result<Object> errorHandler(Exception e, HttpServletRequest httpServletRequest) {
    Result<Object> ret;
    if (e instanceof BaseException) {
      ret = new Result<>(((BaseException) e).getErrorCode(), e.getMessage(), null);
    }
    // 未通过validator 校验异常
    else if (e instanceof BindException) {
      BindingResult result = ((BindException) e).getBindingResult();
      StringJoiner sj = new StringJoiner(",");
      for (FieldError fieldError : result.getFieldErrors()) {
        // 如果绑定失败，那么是类型错误
        if (fieldError.isBindingFailure()) {
          sj.add(fieldError.getField() + "类型错误");
        } else {
          // 没绑定失败就是校验错误
          sj.add(fieldError.getDefaultMessage());
        }
      }
      return Results.error(sj.toString());
    } else {
      ret = new Result<>(ResultCodeEnum.COMMONERROR, "处理出错", null);
    }
    try {
      if (httpServletRequest != null) {
        log.error("error in url:" + httpServletRequest.getRequestURI());
      }
    } catch (Exception exception) {
      log.error("failed to get uri from request", exception);
    }
    log.error("rest api 接口出错", e);
    return ret;
  }

  /**
   * 根据token获取门户用户信息
   */
  public PortalUserInfo getPortalUserInfo() {
    if (StringUtils.isEmpty(this.getToken())) {
      return null;
    }
    return (PortalUserInfo)SessionManager.getObject(this.getToken());
  }

  /**
   * 刷新token对应的门户信息
   */
  public void refreshPortalUserInfo(PortalUserInfo portalUserInfo) {
    SessionManager.setObject(this.getToken(), portalUserInfo);
  }

  public void publishEvent(ApplicationEvent event) {
    this.applicationContext.publishEvent(event);
  }
}

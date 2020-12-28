## 用户上下文

提供每个请求上下文中附带的用户信息.

## 快速开始

### 1. 引入依赖

### 2. 添加配置

```
track.rest.authctx.header-names=user-id,user-name
track.rest.authctx.mocked-values=1122,infilos
```

### 3. 用户上下文校验

- 为特定请求添加校验: 在 Controller 方法上添加注解 `@UserAuthentic`
- 为所有请求添加校验: 在 Controller 实现类上添加注解 `@UserAuthentic`

### 4. 用户上下文检索

- 实现 `AuthctxService` 用户检索实际的用户信息, 或构造模拟用户信息
- 继承 `AuthctxController` 以获取用户信息检索方法
- 通过 `authUser` 方法直接获取当前请求的用户信息
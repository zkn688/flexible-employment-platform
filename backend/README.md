# 灵活就业管理服务平台后端

## 技术栈

```text
Spring Boot 2.7.18
MyBatis Plus 3.5.5
MySQL 8
JDK 8+
```

## 数据库

数据库脚本：

```text
../docs/sql/flexible_employment.sql
```

默认连接配置：

```text
数据库名：flexible_employment
用户名：root
密码：123456
端口：3306
```

## 启动方式

在 IDEA 中打开 `backend` 目录，等待 Maven 依赖加载完成，然后运行：

```text
com.example.employment.EmploymentApplication
```

也可以在命令行启动：

```bash
mvn spring-boot:run
```

服务地址：

```text
http://localhost:8080
```

## 测试账号

```text
用户端：user / 123456
企业端：company / 123456
管理员端：admin / 123456
```

## 用户端接口测试

登录：

```http
POST http://localhost:8080/api/user/login
Content-Type: application/json

{
  "username": "user",
  "password": "123456"
}
```

登录成功后返回：

```text
token: user-1
```

后续用户端接口请求头携带：

```text
Authorization: user-1
```

岗位列表：

```http
GET http://localhost:8080/api/user/jobs?pageNum=1&pageSize=10
```

我的简历：

```http
GET http://localhost:8080/api/user/resumes
Authorization: user-1
```

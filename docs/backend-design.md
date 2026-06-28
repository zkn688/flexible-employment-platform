# 后端接口与实体类规划

## 技术栈

```text
Spring Boot
MyBatis Plus
MySQL
Maven
```

## 推荐包结构

```text
com.example.employment
├─ EmploymentApplication.java
├─ common
│  ├─ Result.java
│  ├─ PageResult.java
│  └─ Constants.java
├─ config
│  ├─ MybatisPlusConfig.java
│  └─ WebConfig.java
├─ controller
│  ├─ user
│  │  ├─ UserAuthController.java
│  │  ├─ UserProfileController.java
│  │  ├─ UserJobController.java
│  │  ├─ UserResumeController.java
│  │  ├─ UserApplicationController.java
│  │  ├─ UserPolicyController.java
│  │  ├─ UserSocialSecurityController.java
│  │  ├─ UserContractController.java
│  │  └─ UserMessageController.java
│  ├─ company
│  └─ admin
├─ entity
├─ mapper
├─ service
│  └─ impl
├─ dto
│  ├─ request
│  └─ response
└─ utils
   └─ TokenUtils.java
```

## 通用返回格式

### Result

```java
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
}
```

建议状态码：

```text
200 成功
400 参数错误
401 未登录或登录过期
403 无权限
404 数据不存在
500 系统异常
```

### PageResult

```java
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Long pageNum;
    private Long pageSize;
}
```

## 实体类映射

| 数据表 | Java 实体类 | 说明 |
|---|---|---|
| `user` | `User` | 用户信息 |
| `company` | `Company` | 企业信息 |
| `admin` | `Admin` | 管理员信息 |
| `job` | `Job` | 岗位信息 |
| `resume` | `Resume` | 简历信息 |
| `application` | `Application` | 简历投递记录 |
| `employment_pref` | `EmploymentPref` | 求职偏好 |
| `favorite_job` | `FavoriteJob` | 岗位收藏 |
| `notice` | `Notice` | 公告通知 |
| `policy` | `Policy` | 政策服务 |
| `policy_apply` | `PolicyApply` | 政策申报 |
| `message` | `Message` | 消息 |
| `social_security_apply` | `SocialSecurityApply` | 社保参保申请 |
| `social_payment_record` | `SocialPaymentRecord` | 社保缴费记录 |
| `labor_contract` | `LaborContract` | 电子劳动合同 |
| `company_review` | `CompanyReview` | 企业评价 |

## Mapper 规划

每张表对应一个 Mapper，继承 MyBatis Plus 的 `BaseMapper<T>`。

```text
UserMapper extends BaseMapper<User>
CompanyMapper extends BaseMapper<Company>
AdminMapper extends BaseMapper<Admin>
JobMapper extends BaseMapper<Job>
ResumeMapper extends BaseMapper<Resume>
ApplicationMapper extends BaseMapper<Application>
EmploymentPrefMapper extends BaseMapper<EmploymentPref>
FavoriteJobMapper extends BaseMapper<FavoriteJob>
NoticeMapper extends BaseMapper<Notice>
PolicyMapper extends BaseMapper<Policy>
PolicyApplyMapper extends BaseMapper<PolicyApply>
MessageMapper extends BaseMapper<Message>
SocialSecurityApplyMapper extends BaseMapper<SocialSecurityApply>
SocialPaymentRecordMapper extends BaseMapper<SocialPaymentRecord>
LaborContractMapper extends BaseMapper<LaborContract>
CompanyReviewMapper extends BaseMapper<CompanyReview>
```

## Service 规划

基础结构：

```text
UserService / UserServiceImpl
CompanyService / CompanyServiceImpl
AdminService / AdminServiceImpl
JobService / JobServiceImpl
ResumeService / ResumeServiceImpl
ApplicationService / ApplicationServiceImpl
EmploymentPrefService / EmploymentPrefServiceImpl
FavoriteJobService / FavoriteJobServiceImpl
NoticeService / NoticeServiceImpl
PolicyService / PolicyServiceImpl
PolicyApplyService / PolicyApplyServiceImpl
MessageService / MessageServiceImpl
SocialSecurityApplyService / SocialSecurityApplyServiceImpl
SocialPaymentRecordService / SocialPaymentRecordServiceImpl
LaborContractService / LaborContractServiceImpl
CompanyReviewService / CompanyReviewServiceImpl
```

建议所有 Service 继承 MyBatis Plus：

```java
public interface UserService extends IService<User> {
}

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
}
```

## 用户端 Controller 规划

### 1. 用户认证 UserAuthController

基础路径：

```text
/api/user
```

| 功能 | 方法 | 地址 | 说明 |
|---|---|---|---|
| 用户注册 | POST | `/register` | 新增用户账号 |
| 用户登录 | POST | `/login` | 校验用户名密码，返回用户信息和 token |

请求 DTO：

```text
UserRegisterRequest
- username
- password
- realName
- phone
- email

UserLoginRequest
- username
- password
```

响应 DTO：

```text
LoginResponse
- id
- username
- realName
- phone
- token
```

### 2. 个人中心 UserProfileController

基础路径：

```text
/api/user/profile
```

| 功能 | 方法 | 地址 | 说明 |
|---|---|---|---|
| 获取当前用户信息 | GET | `/` | 查询用户资料 |
| 修改当前用户信息 | PUT | `/` | 修改手机号、邮箱、姓名等 |
| 修改密码 | PUT | `/password` | 修改登录密码 |

请求 DTO：

```text
UserProfileUpdateRequest
- realName
- gender
- phone
- email
- idCard
- avatar

PasswordUpdateRequest
- oldPassword
- newPassword
```

### 3. 岗位 UserJobController

基础路径：

```text
/api/user/jobs
```

| 功能 | 方法 | 地址 | 说明 |
|---|---|---|---|
| 岗位列表 | GET | `/` | 查询审核通过且上架的岗位 |
| 岗位详情 | GET | `/{id}` | 查看岗位详情，同时可展示企业信息 |
| 推荐岗位 | GET | `/recommend` | 根据求职偏好推荐岗位 |
| 收藏岗位 | POST | `/{id}/favorite` | 收藏岗位 |
| 取消收藏 | DELETE | `/{id}/favorite` | 取消收藏 |
| 我的收藏 | GET | `/favorites` | 查询当前用户收藏岗位 |

查询参数：

```text
keyword
industry
jobType
workCity
salaryMin
salaryMax
pageNum
pageSize
```

岗位列表查询条件：

```text
job.audit_status = 1
job.status = 1
```

### 4. 企业浏览 UserCompanyController

基础路径：

```text
/api/user/companies
```

| 功能 | 方法 | 地址 | 说明 |
|---|---|---|---|
| 企业详情 | GET | `/{id}` | 查看企业资料、岗位数量、评价 |
| 企业评价列表 | GET | `/{id}/reviews` | 查看用户评价 |

企业详情查询条件：

```text
company.audit_status = 1
company.status = 1
```

### 5. 求职偏好 UserPreferenceController

基础路径：

```text
/api/user/preference
```

| 功能 | 方法 | 地址 | 说明 |
|---|---|---|---|
| 查看求职偏好 | GET | `/` | 查询当前用户偏好 |
| 保存或修改求职偏好 | POST | `/` | 若不存在则新增，存在则更新 |

请求 DTO：

```text
PreferenceSaveRequest
- industry
- position
- salaryMin
- salaryMax
- workCity
- jobType
```

### 6. 简历 UserResumeController

基础路径：

```text
/api/user/resumes
```

| 功能 | 方法 | 地址 | 说明 |
|---|---|---|---|
| 我的简历列表 | GET | `/` | 查询当前用户未删除简历 |
| 简历详情 | GET | `/{id}` | 查询单个简历 |
| 新增简历 | POST | `/` | 创建简历 |
| 修改简历 | PUT | `/{id}` | 修改简历 |
| 删除简历 | DELETE | `/{id}` | 软删除，`status=0` |

请求 DTO：

```text
ResumeSaveRequest
- title
- realName
- gender
- birthday
- education
- workYears
- phone
- email
- expectedPosition
- expectedCity
- selfIntro
- attachmentUrl
```

### 7. 投递 UserApplicationController

基础路径：

```text
/api/user/applications
```

| 功能 | 方法 | 地址 | 说明 |
|---|---|---|---|
| 投递岗位 | POST | `/` | 当前用户投递某岗位 |
| 我的投递记录 | GET | `/` | 查询当前用户投递列表 |
| 投递详情 | GET | `/{id}` | 查询投递详情 |
| 撤回投递 | PUT | `/{id}/withdraw` | 仅待处理或已查看可撤回 |

请求 DTO：

```text
ApplicationCreateRequest
- jobId
- resumeId
```

状态值：

```text
0 待处理
1 已查看
2 通过
3 拒绝
4 已撤回
```

业务规则：

```text
同一用户不能重复投递同一岗位
只能投递 audit_status=1 且 status=1 的岗位
只能使用当前用户自己的简历投递
撤回时将 application.status 修改为 4
```

### 8. 公告和政策 UserPolicyController

基础路径：

```text
/api/user
```

| 功能 | 方法 | 地址 | 说明 |
|---|---|---|---|
| 公告列表 | GET | `/notices` | 查询已发布公告 |
| 公告详情 | GET | `/notices/{id}` | 查询公告详情 |
| 政策列表 | GET | `/policies` | 查询已发布政策 |
| 政策详情 | GET | `/policies/{id}` | 查询政策详情 |
| 政策申报 | POST | `/policy-applications` | 提交政策申报 |
| 我的政策申报 | GET | `/policy-applications` | 查询当前用户申报记录 |

政策申报请求 DTO：

```text
PolicyApplyRequest
- policyId
- applicantName
- phone
- materialUrl
```

### 9. 社保 UserSocialSecurityController

基础路径：

```text
/api/user/social-security
```

| 功能 | 方法 | 地址 | 说明 |
|---|---|---|---|
| 社保申请 | POST | `/apply` | 提交灵活就业社保申请 |
| 我的社保申请 | GET | `/applications` | 查询申请记录 |
| 社保缴费记录 | GET | `/payments` | 查询缴费账单和记录 |
| 缴费 | PUT | `/payments/{id}/pay` | 将待缴费账单改为已缴费 |

### 10. 合同 UserContractController

基础路径：

```text
/api/user/contracts
```

| 功能 | 方法 | 地址 | 说明 |
|---|---|---|---|
| 我的电子合同 | GET | `/` | 查询当前用户合同 |
| 合同详情 | GET | `/{id}` | 查询合同详情 |

### 11. 消息 UserMessageController

基础路径：

```text
/api/user/messages
```

| 功能 | 方法 | 地址 | 说明 |
|---|---|---|---|
| 我的消息 | GET | `/` | 查询接收者为当前用户的消息 |
| 发送消息 | POST | `/` | 给企业或管理员发送消息 |
| 标记已读 | PUT | `/{id}/read` | 修改 `is_read=1` |
| 删除消息 | DELETE | `/{id}` | 软删除，`status=0` |

发送消息请求 DTO：

```text
MessageSendRequest
- receiverType
- receiverId
- title
- content
```

## 当前用户 ID 获取方式

课程设计可以先用简单 Token 方案：

```text
用户登录成功后，后端返回 token
前端把 token 保存到 localStorage
Axios 每次请求带上 Authorization 请求头
后端根据 token 解析出 userId
```

也可以先简化为：

```text
前端请求时传 userId
后端根据 userId 查询数据
```

如果时间充足，建议使用 token；如果只想快速完成课设演示，可以先使用 userId 简化开发。

## 用户端开发优先级

建议先完成主流程：

```text
1. 用户注册登录
2. 岗位列表
3. 岗位详情
4. 简历管理
5. 投递岗位
6. 我的投递
7. 个人中心
```

再完成扩展功能：

```text
1. 求职偏好和推荐岗位
2. 收藏岗位
3. 企业详情
4. 公告政策
5. 政策申报
6. 社保服务
7. 电子合同
8. 消息中心
```

# 火车票售票系统

基于 Vue 3、TypeScript、Element Plus、Spring Boot 和 MyBatis-Plus 的数据库课程设计项目，使用 MySQL 存储业务数据，接入支付宝沙箱模拟支付。

## 功能

- 注册、登录、个人信息维护及角色功能入口。
- 按城市或车站查询直达车票、换乘查询、列车线路展示。
- 选座购票、订单查询、支付宝沙箱支付、改签和退票。
- 用户、订单、车站、车次、开行计划、车厢和座位管理。
- 修改相邻站票价后，自动累加更新当前车次各区间的整数票价。

## 目录

```text
train-ticket-server/  Spring Boot 后端
train-ticket-web/     Vue 前端
database/             数据库结构、线路基础数据及虚构演示账号
```

## 环境

- JDK 17 或以上，本项目已用 JDK 21 验证。
- MySQL 8.x。
- Node.js 22.22.2+（22.x）、24.15+（24.x）或 26+；本副本用 Node.js 24.19 验证。
- 后端自带 Maven Wrapper，不要求另装 Maven。首次运行需要联网下载依赖。

## 1. 导入演示数据库

先在 MySQL Workbench 中创建独立数据库，避免影响已有项目数据：

```sql
CREATE DATABASE train_ticket_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE train_ticket_demo;
```

在这个新数据库中按顺序执行：

1. `database/01_schema.sql`：创建全部表、索引及外键。
2. `database/02_routes.sql`：导入车站、车次、停靠线路、车厢、座位、席别和票价。
3. `database/03_demo_accounts_and_schedules.sql`：创建虚构账号和导入当天起连续三天的开行计划。

SQL 文件中的线路和票价用于课程演示，不保证与 12306 实时信息一致。未包含原有用户、订单、车票、支付、退款和座位占用数据。日期过期后可单独再次执行第 3 个文件补齐新的开行日期，它不会重复插入相同账号和日期。

演示账号：

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | demo_admin | Demo123456 |
| 普通用户 | demo_user | Demo123456 |
| 车站工作人员 | demo_staff | Demo123456 |

## 2. 启动后端

在 `train-ticket-server/src/main/resources/` 中，将 `application-local.example.yaml` 复制为 `application-local.yaml`，填写自己的 MySQL 密码。默认数据库是 `train_ticket_demo`；使用其他数据库时可在本地配置中覆盖 `spring.datasource.url`。

`application-local.yaml` 自动加载且已加入 Git 忽略规则，不需要设置额外启动参数。也可以通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 等环境变量配置。

Windows PowerShell：

```powershell
cd train-ticket-server
.\mvnw.cmd spring-boot:run
```

macOS / Linux：

```bash
cd train-ticket-server
sh mvnw spring-boot:run
```

后端默认地址为 `http://localhost:8080`。

## 3. 启动前端

另开终端：

```powershell
cd train-ticket-web
npm ci
npm run dev -- --port 5173 --strictPort
```

浏览器访问 `http://localhost:5173`。默认请求本机 8080 后端；若地址不同，将 `.env.example` 复制为 `.env.local`，设置 `VITE_API_BASE_URL` 后重启前端。Vite 环境变量会暴露给浏览器，不能放私钥。

## 4. 配置支付宝沙箱

不配置支付宝时，可先运行登录、查询和管理功能。模拟支付、改签补退款、退票退款需配置沙箱账号和密钥，并保证订单具有相应支付记录。

在本地 `application-local.yaml` 的 `alipay` 部分填写：

- `app-id`：自己的沙箱应用 ID。
- `private-key`：与该应用匹配的应用私钥。
- `alipay-public-key`：支付宝公钥。
- `return-url`：浏览器支付完成后的返回地址，默认 `http://localhost:5173/orderDetail`。
- `notify-url`：支付宝服务器可访问的异步通知完整地址。

本地测试可启动内网穿透：

```powershell
ngrok http 8080
```

把 `notify-url` 设置为 `https://你的公网域名/payment/notify`，重启后端，并保持 ngrok 运行。此地址不能填 `localhost`。浏览器返回地址与异步通知地址用途不同，不要混用。沙箱使用沙箱买家账号，不使用真实付款账号。

## 验证

```powershell
# 在后端目录执行票价业务测试
.\mvnw.cmd -Dtest=TicketPriceServiceImplTests test

# 在前端目录执行类型检查及生产构建
npm run build
```


## 使用范围

本项目用于课程学习和本机演示，不是生产售票系统。当前角色入口控制主要在前端，后端尚无完整权限认证，用户密码采用课程项目的现有存储方式。不要直接作为公网生产服务部署，也不要存储真实个人信息。

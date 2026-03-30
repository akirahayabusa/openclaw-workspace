# 洛神系统 - Windows 后端运维操作指南

> **适用对象：** 后端运维人员  
> **版本：** v1.0.0  
> **更新时间：** 2026-03-30

---

## 📋 目录

1. [环境准备](#1-环境准备)
2. [安装部署](#2-安装部署)
3. [日常运维](#3-日常运维)
4. [监控管理](#4-监控管理)
5. [故障排查](#5-故障排查)
6. [数据备份](#6-数据备份)
7. [安全配置](#7-安全配置)
8. [常见问题](#8-常见问题)

---

## 1. 环境准备

### 1.1 系统要求

| 项目 | 最低要求 | 推荐配置 |
|------|----------|----------|
| **操作系统** | Windows 10/11 | Windows Server 2019+ |
| **CPU** | 2 核 | 4 核+ |
| **内存** | 4 GB | 8 GB+ |
| **硬盘** | 10 GB 可用空间 | 50 GB+ SSD |
| **网络** | 互联网连接 | 稳定宽带 |

### 1.2 软件依赖

#### 1.2.1 JDK 17 安装

1. **下载 JDK 17**
   ```
   访问：https://adoptium.net/
   选择：Temurin 17 (LTS)
   版本：Windows x64 .msi 安装包
   ```

2. **安装步骤**
   - 双击运行 `.msi` 安装包
   - 选择安装路径（默认：`C:\Program Files\Eclipse Adoptium\jdk-17`）
   - 勾选"设置 JAVA_HOME 环境变量"
   - 点击 Install 完成安装

3. **验证安装**
   ```cmd
   # 打开命令提示符（Win + R -> cmd）
   java -version
   
   # 预期输出：
   # openjdk version "17.0.x"
   ```

4. **配置环境变量**（如果未自动配置）
   ```cmd
   # 设置 JAVA_HOME
   setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17" /M
   
   # 添加到 PATH
   setx PATH "%PATH%;%JAVA_HOME%\bin" /M
   ```

#### 1.2.2 Node.js 安装（前端服务）

1. **下载 Node.js**
   ```
   访问：https://nodejs.org/
   选择：LTS 版本（18.x 或更高）
   ```

2. **安装步骤**
   - 双击运行安装包
   - 勾选"Automatically install the necessary tools"
   - 完成安装

3. **验证安装**
   ```cmd
   node --version
   npm --version
   ```

---

## 2. 安装部署

### 2.1 获取项目代码

#### 方式一：从 Git 克隆

1. **安装 Git**
   ```
   访问：https://git-scm.com/download/win
   下载并安装 Git for Windows
   ```

2. **克隆代码**
   ```cmd
   # 创建项目目录
   mkdir C:\luoshen
   cd C:\luoshen
   
   # 克隆代码
   git clone https://github.com/akirahayabusa/openclaw-workspace.git
   cd openclaw-workspace\luoshen-demo
   ```

#### 方式二：下载压缩包

1. 从 GitHub 下载 ZIP 包
2. 解压到目标目录（如 `C:\luoshen\luoshen-demo`）

### 2.2 配置系统

#### 2.2.1 配置环境变量

1. **创建环境变量文件**
   ```cmd
   # 在 C:\luoshen\luoshen-demo 目录创建 setenv.bat
   notepad setenv.bat
   ```

2. **添加以下内容**
   ```batch
   @echo off
   set DASHSCOPE_API_KEY=your_api_key_here
   set SERVER_PORT=9090
   set JAVA_OPTS=-Xms512m -Xmx1024m
   ```

3. **设置 DashScope API Key**
   - 访问：https://dashscope.console.aliyun.com/
   - 获取 API Key
   - 替换 `your_api_key_here` 为实际的 Key

#### 2.2.2 数据库配置

系统默认使用 H2 嵌入式数据库，无需额外配置。

如需切换到 MySQL/PostgreSQL：
1. 编辑 `application.yml`
2. 修改数据库连接配置

### 2.3 编译项目

#### 2.3.1 下载 Maven

1. **下载 Maven**
   ```
   访问：https://maven.apache.org/download.cgi
   下载：Binary zip archive (apache-maven-3.9.x-bin.zip)
   ```

2. **解压并配置**
   ```cmd
   # 解压到 C:\Program Files\Apache\maven
   
   # 配置环境变量
   setx MAVEN_HOME "C:\Program Files\Apache\maven" /M
   setx PATH "%PATH%;%MAVEN_HOME%\bin" /M
   ```

3. **验证**
   ```cmd
   mvn -version
   ```

#### 2.3.2 编译后端

```cmd
cd C:\luoshen\luoshen-demo\luoshen-admin

# 编译（跳过测试）
mvn clean package -DskipTests

# 编译成功后，JAR 包位于：
# target\luoshen-admin-1.0.0-SNAPSHOT.jar
```

#### 2.3.3 安装前端依赖

```cmd
cd C:\luoshen\luoshen-demo\luoshen-admin-ui

# 安装依赖
npm install

# 如果下载慢，可使用国内镜像
npm config set registry https://registry.npmmirror.com
npm install
```

### 2.4 启动服务

#### 2.4.1 创建启动脚本

**后端启动脚本**（`start-backend.bat`）：
```batch
@echo off
chcp 65001 >nul
title 洛神管理后台

cd /d C:\luoshen\luoshen-demo

:: 设置环境变量
set DASHSCOPE_API_KEY=your_api_key_here
set JAVA_OPTS=-Xms512m -Xmx1024m

:: 启动服务
echo 正在启动洛神管理后台...
java %JAVA_OPTS% -jar luoshen-admin\target\luoshen-admin-1.0.0-SNAPSHOT.jar --server.port=9090

pause
```

**前端启动脚本**（`start-frontend.bat`）：
```batch
@echo off
chcp 65001 >nul
title 洛神前端服务

cd /d C:\luoshen\luoshen-demo\luoshen-admin-ui

echo 正在启动前端服务...
npm run dev -- --host 0.0.0.0 --port 5174

pause
```

#### 2.4.2 启动步骤

1. **启动后端**
   - 双击 `start-backend.bat`
   - 等待启动完成（看到 "Started LuoshenAdminApplication"）

2. **启动前端**
   - 双击 `start-frontend.bat`
   - 等待编译完成

3. **访问系统**
   - 前端地址：http://localhost:5174
   - 后端 API：http://localhost:9090

### 2.5 注册为 Windows 服务（可选）

使用 NSSM 将服务注册为 Windows 服务：

1. **下载 NSSM**
   ```
   访问：https://nssm.cc/download
   解压到 C:\Program Files\nssm
   ```

2. **注册后端服务**
   ```cmd
   "C:\Program Files\nssm\win64\nssm.exe" install LuoshenAdmin ^
     "C:\Program Files\Eclipse Adoptium\jdk-17\bin\java.exe" ^
     "-Xms512m" "-Xmx1024m" ^
     "-jar" "C:\luoshen\luoshen-demo\luoshen-admin\target\luoshen-admin-1.0.0-SNAPSHOT.jar" ^
     "--server.port=9090"
   
   # 设置环境变量
   "C:\Program Files\nssm\win64\nssm.exe" set LuoshenAdmin AppEnvironmentExtra DASHSCOPE_API_KEY=your_key
   
   # 启动服务
   net start LuoshenAdmin
   ```

3. **设置开机自启**
   ```cmd
   sc config LuoshenAdmin start= auto
   ```

---

## 3. 日常运维

### 3.1 服务管理

#### 3.1.1 手动启动/停止

**启动服务：**
```cmd
# 如果是 Windows 服务
net start LuoshenAdmin

# 如果是脚本启动
双击 start-backend.bat
```

**停止服务：**
```cmd
# 如果是 Windows 服务
net stop LuoshenAdmin

# 如果是命令行启动
Ctrl + C
```

#### 3.1.2 重启服务

```cmd
net stop LuoshenAdmin
timeout /t 3
net start LuoshenAdmin
```

### 3.2 日志管理

#### 3.2.1 日志位置

- **后端日志**：控制台输出或 `C:\luoshen\luoshen-demo\logs\`
- **前端日志**：浏览器控制台（F12）

#### 3.2.2 配置日志文件

编辑 `application.yml`：
```yaml
logging:
  file:
    name: C:/luoshen/logs/luoshen-admin.log
  level:
    root: INFO
    io.luoshen: DEBUG
```

#### 3.2.3 查看日志

```cmd
# 实时查看日志（需要安装 PowerShell 或使用 Git Bash）
Get-Content C:\luoshen\logs\luoshen-admin.log -Wait

# 或使用记事本
notepad C:\luoshen\logs\luoshen-admin.log
```

### 3.3 配置更新

#### 3.3.1 更新 API Key

1. 停止服务
2. 编辑 `setenv.bat` 或 `application.yml`
3. 重启服务

#### 3.3.2 更新端口

1. 编辑启动脚本
2. 修改 `--server.port=新端口`
3. 重启服务
4. 更新前端配置（`src/utils/request.ts`）

### 3.4 版本升级

```cmd
# 1. 停止服务
net stop LuoshenAdmin

# 2. 备份数据
xcopy C:\luoshen\luoshen-demo\data C:\luoshen\backup\data /E /I

# 3. 拉取新代码
cd C:\luoshen\luoshen-demo
git pull origin main

# 4. 重新编译
mvn clean package -DskipTests

# 5. 启动服务
net start LuoshenAdmin
```

---

## 4. 监控管理

### 4.1 健康检查

#### 4.1.1 手动检查

```cmd
# 检查后端服务
curl http://localhost:9090/api/admin/dashboard/stats

# 检查前端服务
curl http://localhost:5174
```

#### 4.1.2 端口检查

```cmd
# 查看 9090 端口占用
netstat -ano | findstr :9090

# 查看进程信息
tasklist | findstr <PID>
```

### 4.2 性能监控

#### 4.2.1 JVM 监控

启动时添加 JMX 参数：
```batch
set JAVA_OPTS=-Xms512m -Xmx1024m -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
```

使用 JConsole 或 VisualVM 连接监控。

#### 4.2.2 系统资源监控

使用任务管理器或 Performance Monitor：
```cmd
# 打开性能监视器
perfmon
```

### 4.3 告警配置

使用 Windows 任务计划程序 + 脚本实现简单告警：

**创建健康检查脚本**（`health-check.bat`）：
```batch
@echo off
curl -s http://localhost:9090/api/admin/dashboard/stats >nul
if %errorlevel% neq 0 (
    echo 服务异常！ >> C:\luoshen\logs\health-check.log
    :: 可添加邮件通知或其他告警方式
)
```

配置任务计划每 5 分钟执行一次。

---

## 5. 故障排查

### 5.1 服务无法启动

#### 问题：端口被占用

```cmd
# 查看端口占用
netstat -ano | findstr :9090

# 结束占用进程
taskkill /PID <进程ID> /F

# 或更换端口
java -jar xxx.jar --server.port=9091
```

#### 问题：Java 版本不对

```cmd
# 检查 Java 版本
java -version

# 确认是 17 或以上版本
# 如果不是，检查 JAVA_HOME 环境变量
echo %JAVA_HOME%
```

### 5.2 API 请求失败

#### 问题：404 Not Found

- 检查 API 路径是否正确
- 确认服务已完全启动
- 查看后端日志

#### 问题：500 Internal Server Error

- 查看后端日志定位具体错误
- 检查数据库连接
- 检查 API Key 是否有效

### 5.3 数据库问题

#### H2 数据库访问

1. **访问 H2 控制台**
   ```
   浏览器访问：http://localhost:9090/h2-console
   JDBC URL: jdbc:h2:file:C:/luoshen/luoshen-demo/data/luoshen-admin
   User: SA
   Password: (空)
   ```

2. **数据库文件位置**
   ```
   C:\luoshen\luoshen-demo\data\luoshen-admin.mv.db
   ```

3. **重置数据库**
   ```cmd
   # 停止服务
   # 删除数据库文件
   del C:\luoshen\luoshen-demo\data\luoshen-admin.*
   # 重启服务（会自动创建新数据库）
   ```

### 5.4 常见错误码

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| Connection refused | 服务未启动 | 启动后端服务 |
| 401 Unauthorized | API Key 无效 | 检查/更新 API Key |
| OutOfMemoryError | 内存不足 | 增加 -Xmx 参数 |
| BindException | 端口被占用 | 更换端口或结束占用进程 |

---

## 6. 数据备份

### 6.1 手动备份

**创建备份脚本**（`backup.bat`）：
```batch
@echo off
set BACKUP_DIR=C:\luoshen\backup
set DATE=%date:~0,4%%date:~5,2%%date:~8,2%
set TIME=%time:~0,2%%time:~3,2%

:: 创建备份目录
mkdir %BACKUP_DIR%\%DATE%_%TIME%

:: 备份数据库
xcopy C:\luoshen\luoshen-demo\data %BACKUP_DIR%\%DATE%_%TIME%\data /E /I

:: 备份配置
copy C:\luoshen\luoshen-demo\luoshen-admin\src\main\resources\application.yml %BACKUP_DIR%\%DATE%_%TIME%\

echo 备份完成：%BACKUP_DIR%\%DATE%_%TIME%
pause
```

### 6.2 定时自动备份

使用 Windows 任务计划程序：

1. 打开"任务计划程序"
2. 创建基本任务
3. 设置触发器（如每天凌晨 2 点）
4. 操作：启动程序 `backup.bat`

### 6.3 数据恢复

```cmd
# 1. 停止服务
net stop LuoshenAdmin

# 2. 恢复数据
xcopy C:\luoshen\backup\20260330_0200\data C:\luoshen\luoshen-demo\data /E /I /Y

# 3. 启动服务
net start LuoshenAdmin
```

---

## 7. 安全配置

### 7.1 防火墙配置

#### 7.1.1 开放必要端口

```cmd
# 开放后端端口（仅内网）
netsh advfirewall firewall add rule name="Luoshen Admin" dir=in action=allow protocol=tcp localport=9090 remoteip=localsubnet

# 开放前端端口
netsh advfirewall firewall add rule name="Luoshen Frontend" dir=in action=allow protocol=tcp localport=5174
```

#### 7.1.2 限制访问来源

如需限制特定 IP 访问：
```cmd
netsh advfirewall firewall add rule name="Luoshen Admin" dir=in action=allow protocol=tcp localport=9090 remoteip=192.168.1.0/24
```

### 7.2 安全加固

#### 7.2.1 禁用不必要的端点

编辑 `application.yml`：
```yaml
# 禁用 H2 控制台（生产环境）
spring:
  h2:
    console:
      enabled: false

# 或添加访问限制
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

#### 7.2.2 HTTPS 配置（可选）

1. 生成证书
2. 配置 SSL
3. 重定向 HTTP 到 HTTPS

### 7.3 访问控制

#### 7.3.1 添加认证

1. 编辑 `application.yml`：
```yaml
spring:
  security:
    user:
      name: admin
      password: your_secure_password
```

2. 重启服务

---

## 8. 常见问题

### Q1: 服务启动很慢？

**A:** 可能是内存不足或网络问题。
- 增加 JVM 内存：`-Xmx2048m`
- 检查网络连接（首次启动可能需要下载依赖）

### Q2: 前端无法连接后端？

**A:** 检查以下几点：
1. 后端是否已启动
2. 防火墙是否开放端口
3. 前端配置的 API 地址是否正确

### Q3: 数据丢失了怎么办？

**A:** 
1. 检查是否有备份
2. 如果没有，可能无法恢复
3. 建议设置定时备份

### Q4: 如何查看运行中的 Java 进程？

**A:**
```cmd
# 使用 jps 命令
jps -l

# 或使用任务管理器查看 java.exe
```

### Q5: 如何更改数据库存储位置？

**A:** 修改 `application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:h2:file:D:/luoshen-data/luoshen-admin
```

---

## 📞 技术支持

如遇到无法解决的问题，请联系：

- **项目仓库：** https://github.com/akirahayabusa/openclaw-workspace
- **文档地址：** 项目 `docs/` 目录

---

**文档版本：** v1.0.0  
**最后更新：** 2026-03-30  
**维护团队：** 洛神开发团队
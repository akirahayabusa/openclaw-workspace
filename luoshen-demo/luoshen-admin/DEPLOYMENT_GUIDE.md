# 洛神平台技能系统 - 部署指南

## 部署环境要求

### 系统要求

- **操作系统**：Linux/macOS/Windows (WSL 2)
- **Java**：JDK 17+
- **Maven**：3.8+
- **内存**：至少 2GB RAM
- **磁盘**：至少 1GB 可用空间

### 软件依赖

- **数据库**：H2（嵌入式，无需额外安装）
- **应用服务器**：内嵌于 Spring Boot
- **构建工具**：Maven

## 部署步骤

### 1. 获取代码

```bash
# 克隆仓库
git clone https://github.com/akirahabusa/openclaw-workspace.git
cd openclaw-workspace

# 切换到项目目录
cd luoshen-demo
```

### 2. 配置 API Key

编辑配置文件：
```bash
vi luoshen-admin/src/main/resources/application.yml
```

设置 API Key：
```yaml
luoshen:
  model:
    provider: dashscope
    dashscope:
      apiKey: your-api-key-here
      modelName: qwen-max
```

或使用环境变量：
```bash
export DASHSCOPE_API_KEY=your-api-key-here
```

### 3. 编译项目

```bash
mvn clean install
```

### 4. 启动服务

#### 方式 1：前台运行（开发模式）

```bash
cd luoshen-admin
mvn spring-boot:run
```

#### 方式 2：后台运行（生产模式）

```bash
nohup java -jar luoshen-admin/target/luoshen-admin-1.0.0-SNAPSHOT.jar \
  --server.address=0.0.0.0 \
  --server.port=9090 \
  > /var/log/luoshen-admin.log 2>&1 &
echo $! > /var/log/luoshen-admin.pid
```

### 5. 验证部署

```bash
# 检查服务状态
curl http://localhost:9090/api/admin/skills

# 检查首页
curl http://localhost:9090/

# 查看日志
tail -f /var/log/luoshen-admin.log
```

## 生产环境部署

### 使用 Systemd

创建服务文件：
```bash
sudo vi /etc/systemd/system/luoshen-admin.service
```

内容：
```ini
[Unit]
Description=Luoshen Admin Service
After=network.target

[Service]
Type=simple
User=luoshen
WorkingDirectory=/opt/luoshen
ExecStart=/usr/bin/java -jar /opt/luoshen/luoshen-admin/target/luoshen-admin-1.0.0-SNAPSHOT.jar
  --server.address=0.0.0.0
  --server.port=9090
  --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

启用服务：
```bash
sudo systemctl daemon-reload
sudo systemctl enable luoshen-admin
sudo systemctl start luoshen-admin
sudo systemctl status luoshen-admin
```

### 使用 Docker

创建 `Dockerfile`：
```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/luoshen-admin-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/app/luoshen-admin.jar"]
```

构建和运行：
```bash
# 构建
docker build -t luoshen-admin .

# 运行
docker run -d \
  -p 9090:9090 \
  -e DASHSCOPE_API_KEY=your-key \
  luoshen-admin
```

### 使用 Nginx 反向代理

配置 Nginx：
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:9090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

重启 Nginx：
```bash
sudo nginx -t
sudo systemctl reload nginx
```

## 配置优化

### 内存配置

编辑 `luoshen-admin/src/main/resources/application.yml`：

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

server:
  tomcat:
    max-threads: 200
    max-connections: 8192
```

### 文件上传大小限制

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
```

### H2 数据库持久化

编辑 `luoshen-admin/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/luoshen-prod;DB_CLOSE_ON_EXIT=FALSE
    # 或使用 MySQL
    # url: jdbc:mysql://localhost:3306/luoshen
    username: luoshen
    password: password
```

## 监控和日志

### 日志级别配置

```yaml
logging:
  level:
    io.luoshen: DEBUG
    org.springframework: INFO
    org.hibernate: INFO
  file:
    name: /var/log/luoshen-admin.log
  pattern:
    "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 监控端点

Spring Boot Actuator：
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

访问：`http://localhost:9090/actuator/health`

### 日志管理

```bash
# 查看日志
tail -f /var/log/luoshen-admin.log

# 查看最近 100 行
tail -n 100 /var/log/luoshen-admin.log

# 搜索错误
grep ERROR /var/log/luoshen-admin.log
```

## 备份和恢复

### 数据备份

#### 备份数据库

```bash
# 备份 H2 数据文件
cp ~/.luoshen/data/luoshen-prod.mv ~/.luoshen/data/luoshen-prod.backup.mv
```

#### 备份技能包

```bash
# 备份技能目录
tar -czf luoshen-skills-backup.tar.gz ~/.luoshen/skills/
```

### 数据恢复

#### 恢复数据库

```bash
# 恢复 H2 数据
cp ~/.luoshen/data/luoshen-prod.backup.mv ~/.luoshen/data/luoshen-prod.mv
```

#### 恢复技能包

```bash
# 解压技能包
tar -xzf luoshen-skills-backup.tar.gz -C ~/
```

## 性能优化

### JVM 参数优化

```bash
java -jar luoshen-admin-1.0.0-SNAPSHOT.jar \
  -Xms512m \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:+UseG1MMCSSAAClassLoaderDisable \
  -XX:+UseStringDeduplicationStrings \
  -XX:+UseCompressedOops \
  -XX:+OptimizeStringConcat
  -XX:+OptimizeUnnecessary
```

### 连接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
```

## 故障排除

### 服务无法启动

**检查端口占用：**
```bash
ss -tuln | grep 9090
```

**查看日志：**
```bash
tail -f /var/log/luoshen-admin.log
```

**常见错误：**
- `BindException: Address already in use` → 端口被占用
- `Connection refused` → 服务未启动或配置错误
- `API Key 无效` → 检查配置文件或环境变量

### 技能无法加载

**检查：**
1. 技能是否启用
2. Agent 是否绑定了技能
3. 文件权限问题

**查看日志：**
```bash
grep "加载技能" /var/log/luoshen-admin.log
grep "AgentSkillLoader" /var/log/ upgrading
```

### 内存不足

**增加 JVM 内存：**
```bash
java -Xmx2g -jar luoshen-admin.jar
```

### 磁盘空间不足

**清理临时文件：**
```bash
# 清理上传的临时文件
rm -rf ~/.luoshen/packages/uploads/temp_*

# 清理旧日志
journalctl --vacuum-time=30d
```

## 升级指南

### 升级版本

```bash
# 停止服务
sudo systemctl stop luoshen-admin

# 备份当前版本
cp luoshen-admin.jar luoshen-admin.jar.bak

# 部署新版本
cp /path/to/new/luoshen-admin.jar luoshen-admin.jar

# 启动服务
sudo systemctl start luoshen-admin

# 验证
curl http://localhost:9090/actuator/health
```

### 数据库迁移

```bash
# 导出数据
curl http://localhost:9090/actuator/health > backup.json

# 导入数据
curl -X POST http://localhost:9090/actuator/configprops \
  -H "Content-Type: application/json" \
  -d @backup.json
```

## 安全配置

### 配置 HTTPS

在 `application.yml` 中配置：
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-alias: tomcat
```

### 配置防火墙

```bash
# 开放端口
sudo ufw allow 9090

# 查看状态
sudo ufw status
```

### 配置访问控制

编辑 `application.yml`：
```yaml
spring:
  security:
    user:
      name: admin
      password: $2a$10$...
```

---

**文档版本：** v1.0
**更新时间：** 2026-04-01
**状态：** Phase 5 - 部署文档完成

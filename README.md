# Account-Book 记账本应用

## 项目简介
基于Java开发的个人记账本应用，采用MVC架构设计，帮助用户记录和管理日常收支情况。

## 功能特性
- 📅 查看收支（根据年/月/日进行范围查询）
- 📊 统计报表
- 💰 收支记录管理（增删改查/批量导入/批量导出）
- 🤖 智能消费分类 消费建议
- 📁 CSV文件路径迁移
- ⚙️ 账户设置(密码、性别、年龄)

## 项目结构
![项目结构](https://github.com/liusdeorange/Software-project-24/Account-Book)


### 🗂 核心模块说明

| 目录                | 功能描述 |
|---------------------|--------------------------|
| **controller**     | 处理用户操作，调用业务逻辑 |
| **model**          | 数据实体类(user/csv/account)|
| **test**           | 测试代码|
| **util**           | 工具类(Ai分类/MD5)|
| **view**           | 用户界面实现       |
| **resources**      | 静态资源与配置文件|

### 📝 配置文件说明
- `archetype-resources`:生成Archetype项目必须需的模板文件和目录结构
- `META-INF.maven`:maven项目结构生成
- `AccountBook.png`:程序内图图标
- `config.properties`:包含默认账目文件存储路径
- `testUser_finance.csv`:测试用户的CSV数据（字段：日期,分类,金额,备注）

## 🚀 快速开始
### 🛠 开发环境
- JDK 21
- Maven 3.9+

### 使用说明
#### 1.克隆项目[Account-Book](https://github.com/liusdeorange/Software-project-24/tree/main/Account-Book)
#### 2.使用maven构建项目
#### 3.直接编译运行**main.java**<ins> (Account-Book/src/main/java/main.java)</ins>

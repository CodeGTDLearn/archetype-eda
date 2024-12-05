# 🔐 Secure WebService Project

## 1 Description:
> A robust web application focused on security, logging, and type-safe configuration management.

## 2 System Design

This project implements a secure webservice with emphasis on credential protection, comprehensive logging, and type-safe configuration management. It follows best practices for security and maintainability.

## 3 Technical Specifications
- [Credentials Protection](#credentials-protection-top)
- [Advanced Logging System](#advanced-logging-system-top)
- [Type-Safe Configuration](#type-safe-configuration-top)

---

### 🔐Credentials Protection [[top]](#3-technical-specifications)

#### ✅ Strategy based on:
- OS Encrypted Folder Vault
- Global GitIgnore Implementation
- Project Protected Properties Folder

#### 🔒 OS Encrypted Folder Vault
- Utilizes Windows 10 BitLocker for encryption
- Stores all credential files iindividually
- Automatic encryption of all content (files + subfolders)
- Completely separated from project structure
- Environment variable integration with **_"Project Protected Properties Folder"_**

#### 🚫 Global GitIgnore Implementation
- Centralized sensitive file exclusion
- Keyword-based file exclusion, such as:
- CLI Configuration:
  ```bash
  # Create new global gitignore (if not exists yet)
  git config --global core.excludesfile C:\your-path\.gitignore_global
  
  # Find existing file
  git config --global core.excludesfile
  ```

#### 🛡️ Project Protected Properties Folder
- Project Dedicated folder for sensitive properties
- Environment variable integration with _**"OS Encrypted Folder Vault"**_
- Localized inside the project

---

### 📄Advanced Logging System [[top]](#3-technical-specifications)

#### ✅ Strategy based on:
- **Multi-layer Logging Architecture**

#### 📜Logback Integration
- Customized logback-spring and logback-test implementations
- Development and test environment console banners

#### 🎯 Console Appenders Types
1. Default Spring
2. Tabular
3. Granular

#### 📊 Extended Logging Capabilities
- Email notification system
- RollingFile storage
  - Classic mode
  - Rolling mode
  - Logstash format
- TCP-SOCKETS for logstash
  - CPanel integration

#### 📈 Logging Hierarchy
```
ERROR > WARN > INFO > DEBUG > TRACE
```
---

### 🛠️Type-Safe Configuration [[top]](#3-technical-specifications)

#### ✅ Strategy based on:
- **Externalized Properties/Configuration Binding**

#### 🔄 Binding Types
1. 🏗️Constructor Binding (Immutable)
    - Record-based implementation
    - Class-based implementation with final Attributes
2. 🔧JavaBean Binding (Mutable)
    - Standard class implementation
    - Setter-based configuration

#### 🔍 Context Scanning
1. Combined scanning for Constructor and JavaBean Binding:
  * EnableConfigurationProperties
  * _ConfigurationPropertiesScan_
2. Specialized JavaBean Binding scanning:
  * Stereotype Annotations: Component or Configuration

#### 📁📄 Supported Filetypes
- Properties files
- YAML configuration

#### 🎨 Binding Formats
  - Simple properties
  - List
  - Map

#### ✔️ Validation
- Spring-Validation integration

---


---
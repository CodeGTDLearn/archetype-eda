<!-- ====================================================
     1. Cabeçalho e Informações Iniciais
======================================================= -->
<h1> 🔐 ${artifactId} WebService Project </h1>
<h2> 1 Description </h2>
<p> ${project_description} </p>

<h2> 2 System Design </h2>
<p>
  This project implements a secure webservice with emphasis on credential protection, comprehensive logging, and type-safe configuration management. It follows best practices for security and maintainability.
</p>

<h2 id="technical-specifications">3 Technical Specifications</h2>
<ul>
  #if(${enable_lombok} == "true")
    <li>Lombok</li>
  #end
  #if(${enable_docker} == "true")
    <li>Docker Support</li>
  #end
  #if(${enable_git_support} == "true")
    <li>Git Repo: <a href="${git_url_repo}">${git_url_repo}</a></li>
  #end
  <li><a href="#credentials-protection-top">Credentials Protection</a></li>
  #if(${enable_logback} == "true")
    <li><a href="#advanced-logging-system-top">Advanced Logging System</a></li>
  #end
  #if(${enable_binding_properties} == "true")
    <li><a href="#type-safe-binding-configuration-top">Type-Safe Binding Configuration</a></li>
  #end
</ul>
<hr>


<!-- ====================================================
     2. Seção: Credentials Protection (Collapse Simples)
======================================================= -->
<details>
  <summary id="credentials-protection-top">
    <b>
      🔐 Credentials Protection <a href="#technical-specifications">[top]</a>
    </b>
  </summary>
  <div>
    <h4> ✅ Strategy based on: </h4>
    <ul>
      <li> OS Encrypted Folder Vault </li>
      <li> Global GitIgnore Implementation </li>
      <li> Project Protected Properties Folder </li>
    </ul>
    <h4> 🔒 OS Encrypted Folder Vault </h4>
    <ul>
      <li> Utilizes Windows 10 BitLocker for encryption </li>
      <li> Stores all credential files individually </li>
      <li> Automatic encryption of all folder-content (files + subfolders) </li>
      <li> Completely separated from project structure </li>
      <li> Environment variable integration with <b>"Project Protected Properties Folder"</b> </li>
    </ul>
    <h4> 🚫 Global GitIgnore Implementation </h4>
    <ul>
      <li> Centralized sensitive file exclusion </li>
      <li> Keyword-based file exclusion, such as: </li>
      <li> CLI Configuration: </li>
    </ul>
    <ul>
      <li> Create new global gitignore (if not exists yet) </li>
      <pre>  git config --global core.excludesfile C:\your-path\.gitignore_global </pre>
      <li> Find existing file </li>
      <pre> git config --global core.excludesfile  </pre>
    </ul>
    <h4> 🛡️ Project Protected Properties Folder </h4>
    <ul>
      <li> Project Dedicated folder for sensitive properties </li>
      <li>Environment variable integration with <i>"OS Encrypted Folder Vault"</i></li>
      <li> Localized inside the project </li>
    </ul>
  </div>
</details>
<hr>

#if(${enable_logback} == "true")
<!-- ====================================================
     3. Seção: Advanced Logging System (Collapse Simples)
======================================================= -->
<details>
  <summary id="advanced-logging-system-top">
    <b>
      📄 Advanced Logging System <a href="#technical-specifications">[top]</a>
    </b>
  </summary>
  <div>
    <h4> ✅ Strategy based on: </h4>
    <ul>
      <li> Multi-layer Logging Architecture </li>
    </ul>
    <h4> 📜Logback Integration </h4>
    <ul>
      <li> Customized logback-spring and logback-test implementations </li>
      <li> Development and test environment console banners </li>
    </ul>
    <h4> 🎯 Console Appenders Types </h4>
    <ul>
      <li> Default Spring </li>
      <li> Tabular </li>
      <li> Granular </li>
    </ul>
    <h4> 📊 Extended Logging Capabilities </h4>
    <ul>
      <li> Email notification system </li>
      <li> RollingFile storage </li>
      <ul>
        <li> Classic mode </li>
        <li> Rolling mode </li>
      </ul>
      <li> Logstash format </li>
      <ul>
        <li> TCP-SOCKETS for logstash </li>
      </ul>
      <li> CPanel integration </li>
    </ul>
    <h4> 📈 Logging Hierarchy </h4>
    <pre>
      ERROR > WARN > INFO > DEBUG > TRACE
    </pre>
  </div>
</details>
<hr>
#end

#if(${enable_binding_properties} == "true")
<!-- ====================================================
     4. Seção: Type-Safe Binding Configuration (Collapse Simples)
======================================================= -->
<details>
  <summary id="type-safe-binding-configuration-top">
    <b>
      🛠️ Type-Safe Binding Configuration <a href="#technical-specifications">[top]</a>
    </b>
  </summary>
  <div>
    <h4> ✅ Strategy based on: </h4>
    <ul>
      <li> Externalized Properties/Configuration Binding </li>
    </ul>
    <h4> 🔄 Binding Types </h4> 
    <ul>
      <li> 🏗️Constructor Binding (Immutable) </li>
      <ul>
        <li> Record-based implementation </li>
        <li> Class-based implementation with final Attributes </li>
      </ul>
      <li> 🔧JavaBean Binding (Mutable) </li>
      <ul>
        <li> Standard class implementation </li>
        <li> Setter-based configuration </li>
      </ul>
    </ul>
    <h4> 🔍 Context Scanning </h4>
    <ul>
      <li> Combined scanning for Constructor and JavaBean Binding: </li> 
      <ul>
        <li> EnableConfigurationProperties  </li> 
        <li> ConfigurationPropertiesScan </li>
      </ul>
      <li> Specialized JavaBean Binding scanning: </li> 
      <ul>
        <li> Stereotype Annotations: Component or Configuration </li> 
      </ul>
    </ul>
    <h4> 📁📄 Supported Filetypes </h4>
    <ul>
      <li> Properties files </li>
      <li> YAML configuration </li>
    </ul>
    <h4> 🎨 Binding Formats </h4>
    <ul>
      <li> Simple properties </li>
      <li> List </li>
      <li> Map </li>
    </ul>
    <h4> ✔️ Validation </h4>
    <ul>
      <li> Spring-Validation integration </li>
    </ul>
  </div>
</details>
<hr>
#end
<div align="center">
<h1>HiMiuix</h1>

![stars](https://img.shields.io/github/stars/HChenX/HiMiuix?style=flat)
![Github repo size](https://img.shields.io/github/repo-size/HChenX/HiMiuix)
![last commit](https://img.shields.io/github/last-commit/HChenX/HiMiuix?style=flat)
![language](https://img.shields.io/badge/language-java-purple)

[//]: # (<p><b><a href="README-en.md">English</a> | <a href="README.md">简体中文</a></b></p>)
<p>仿 Miuix 的 xml 式组件</p>
</div>

### HiMiuiX

#### Demo 项目:

- [HiMiuixDemo](https://github.com/HChenX/HiMiuixDemo)
- 欢迎下载体验！

#### 展示:

- Miuix About
  ![MiuixAbout](https://raw.githubusercontent.com/HChenX/HiMiuix/master/image/miuix.jpg)

### 使用

- 请做为模块在项目中导入使用！

```shell
  # 在你仓库中执行，将本仓库作为模块使用
  git submodule add https://github.com/HChenX/HiMiuix.git
  # 后续拉取本模块仓库
  git submodule update --init
```

- 然后设置项目 settings.gradle 添加:

```groovy
include ':HiMiuix'
```

- 最后设置项目 app 下 build.gradle 文件，添加:

```groovy
implementation(project(':HiMiuix'))
```

- tip: 请确保导入并使用了 `com.android.library`

#### 开源许可

- 本项目遵循 LGPL-2.1 开源协议。
- 使用本项目请在项目中注明！

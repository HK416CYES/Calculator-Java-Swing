# 科学计算器

这是一个基于 Java Swing 开发的桌面科学计算器，支持基础四则运算、常见科学函数、历史记录、记忆功能以及键盘快捷输入。

## 运行环境

- JDK 或 JRE 17 及以上版本
- Windows、macOS、Linux 均可运行

## Windows 平台运行方式

项目已经提供 Windows 发布目录，进入 `release/windows/ScientificCalculator` 后：

- 双击 `启动科学计算器.bat`
- 或在命令行中执行 `java -jar ScientificCalculator.jar`

如果双击没有反应，请先确认系统已经安装 Java，并且 `java` 命令可用。

## 主要功能

- 基础运算：加、减、乘、除
- 科学函数：`sin`、`cos`、`tan`、`log`、`ln`、`sqrt`、平方、倒数
- 表达式能力：括号、乘方、百分比、正负号切换
- 常量：`pi`、`e`
- 历史记录：查看、回放、导出、清空
- 记忆功能：`MC`、`MR`、`M+`、`M-`、`MS`
- 结果回填：`Ans`

## 界面使用说明

### 顶部区域

- 上方显示当前表达式
- 下方显示当前结果
- 右上角显示当前记忆值状态
- 最下方状态栏显示操作反馈或错误信息

### 控制区

- `MC`：清空记忆值
- `MR`：回填记忆值到表达式
- `M+`：将当前值加到记忆值
- `M-`：从记忆值中减去当前值
- `MS`：将当前值写入记忆值
- `CE`：清空当前表达式
- `C`：清空表达式和结果
- `⌫`：删除最后一个输入字符
- `复制表达式`：复制当前表达式
- `复制结果`：复制当前结果

### 科学功能区

- `sin`、`cos`、`tan`：三角函数，默认使用弧度制
- `log`：以 10 为底对数
- `ln`：自然对数
- `sqrt`：平方根
- `x²`：平方
- `1/x`：倒数
- `%`：百分比
- `^`：乘方
- `(`、`)`：括号
- `π`、`e`：常量
- `Ans`：将最近一次结果插入当前表达式

### 数字与运算区

- 数字键：输入数字
- `+/-`：切换当前表达式的正负号
- `.`：输入小数点
- `+`、`-`、`×`、`÷`：基础运算
- `=`：执行求值

## 键盘快捷输入

- 数字键 `0-9`：输入数字
- `+`、`-`、`*`、`/`、`^`：输入运算符
- `(`、`)`：输入括号
- `.`：输入小数点
- `%`：输入百分比
- `Enter`：求值
- `Backspace`：删除最后一个字符
- `Esc`：清空表达式和结果
- `s`、`c`、`t`、`l`、`n`：快速输入 `sin(`、`cos(`、`tan(`、`log(`、`ln(`
- `p`：输入 `pi`
- `e`：输入常量 `e`
- `q`：对当前表达式应用 `sqrt`
- `r`：对当前表达式应用倒数
- `x`：对当前表达式应用平方

## 历史记录与状态保存

- 计算结果会自动加入右侧历史记录列表
- 双击历史记录可回放该表达式
- 历史记录支持导出为文本文件
- 程序会自动保存历史记录、最近结果和记忆值，下次启动时自动恢复

## 重新打包发布

项目提供脚本 `scripts/package_windows_release.sh` 用于生成 Windows 发布目录。执行：

```bash
sh scripts/package_windows_release.sh
```

脚本会自动完成编译、打包，并生成：

- `release/windows/ScientificCalculator/ScientificCalculator.jar`
- `release/windows/ScientificCalculator/启动科学计算器.bat`
- `release/windows/ScientificCalculator/README.txt`
- `release/windows/ScientificCalculator.zip`

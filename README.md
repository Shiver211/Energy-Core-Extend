# Energy Core Extend

为龙之研究 1.12.2 增加九级无限储能核心，并将一至九级核心的每个输入输出口提升至 `Long.MAX_VALUE` RF/t。

- 每个口的所有方向、RF/FE 和 long 调用共享每 tick 额度；不同端口独立计量。
- 一至八级继续受原有容量限制，九级保留无限储能和现有存档格式。
- 通量网络适配。将通量插头和通量点设为取消限制，即可使用完整传输上限。
- 普通 RF/FE 单次调用仍受 `Integer.MAX_VALUE` 限制。

客户端和服务端均需安装本模组、龙研及其依赖、MixinBooter。

## long 接口

能量核心和输入输出口实现 `IExtendedEnergyCore`。外部传输应通过输入输出口调用，端口负责模式、结构、激活状态和每 tick 额度检查，核心负责储能。

```java
IExtendedEnergyCore port = (IExtendedEnergyCore) pylon;
long accepted = port.receiveEnergyLong(Long.MAX_VALUE, true); // 模拟，不改变储能或额度
accepted = port.receiveEnergyLong(accepted, false);           // 返回实际接收数量
long extracted = port.extractEnergyLong(Long.MAX_VALUE, false);
BigInteger stored = port.getStoredBig();                     // 完整储能，不截断到 long
```

## 构建与测试

```powershell
.\gradlew.bat test build
```

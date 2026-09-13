# 门锁 Locksmith（NeoForge 1.21.1）

一个简单的“门锁”模组：右键任意门就能上锁，配一把钥匙就能开锁。

- 版本：Minecraft **1.21.1** / NeoForge **21.1.235**
- 加载器：NeoForge（Java 版，`javafml`）
- 环境：**单机、服务器、客户端都要装**（物品是通用内容，没有单独的客户端代码）

---

## 1. 玩法

| 操作 | 效果 |
| --- | --- |
| 手持任意锁，**右键** 门 / 活板门 / 栅栏门 | 上锁（消耗一个锁，锁会记住主人） |
| 手持**钥匙**，**潜行 + 右键**已上锁的门 | 绑定钥匙（只有锁的主人或 OP 能绑定） |
| 手持**已绑定的钥匙**，**右键**对应的门 | 开锁，锁会返还给主人 |
| 手持别的东西（或空手）右键已上锁的门 | 打不开，会提示“已上锁（主人：xxx）” |
| 破坏已上锁的门 | 默认只有主人能破坏，锁会掉落返还 |

特点：

- **双开门会一起上锁**：点一半，两扇门都会锁上（否则留一半门就能钻过去）。
- **已上锁的门的红石免疫**：按钮 / 拉杆 / 压力板都开不了，防止绕过锁。
- **兼容各种模组的门**：麦考（Macaw's Doors / Trapdoors / Fences）等模组的门基本都是原版 `DoorBlock`
  的子类，直接就能用，不需要额外配置。

### 物品

| 物品 | 说明 | 配方 |
| --- | --- | --- |
| 木锁 / 石锁 / 铁锁 / 金锁 / 钻石锁 / 下界合金锁 | 安全等级 1~6，等级只影响提示（以后会用于撬锁玩法） | `2x2`：3 个材料 + 1 个铁粒（下界合金锁 = 1 下界合金锭 + 1 铁锁） |
| 钥匙 | 未绑定时右键无效，潜行右键已上锁的门即可绑定 | 1 铁粒 + 1 金粒（竖着摆） |
| 万能钥匙 | 管理员工具，能打开任何锁 | 无配方，只在创造模式物品栏 |

绑定的钥匙会带上附魔光效，方便区分。

---

## 2. 配置文件

首次启动后生成 `config/locksmith-common.toml`：

| 配置项 | 默认 | 说明 |
| --- | --- | --- |
| `lockedBlocksIgnoreRedstone` | `true` | 已上锁的门是否免疫红石 |
| `othersCanBreakLockedBlocks` | `false` | 其他玩家能否破坏已上锁的门（OP 始终可以） |
| `dropLockWhenBroken` | `true` | 门被破坏时是否掉落锁 |
| `returnLockOnUnlock` | `true` | 开锁时是否把锁返还给主人 |
| `onlyOwnerCanBindKey` | `true` | 是否只有主人能绑定钥匙 |
| `keyBindingNeedsSneak` | `true` | 绑定钥匙是否需要潜行 |
| `showLockedHint` | `true` | 右键已上锁的门时是否提示 |

---

## 3. 兼容“完全自定义的门”

默认情况下，只要是原版 `DoorBlock` / `TrapDoorBlock` / `FenceGateBlock` 的子类（绝大多数模组都是），
就已经可以上锁了。如果某个模组完全自己写了门方块，用数据包把它加进方块标签即可：

```json
// data/<你的命名空间>/tags/blocks/lockable.json
{
  "replace": false,
  "values": [
    "某个模组:它的门"
  ]
}
```

标签里默认已经包含 `#minecraft:doors`、`#minecraft:trapdoors`、`#minecraft:fence_gates`。

---

## 4. 构建 / 运行

```powershell
.\gradlew.bat runClient     # 启动带模组的客户端
.\gradlew.bat runServer     # 启动服务端
.\gradlew.bat build         # 打包，产物在 build/libs/locksmith-1.0.0.jar
```

> 首次构建需要联网下载 NeoGradle + NeoForge + Minecraft（几百 MB），会慢一些。

构建产物已验证：

- `gradlew build` 通过，产物 `build/libs/locksmith-1.0.0.jar`
- `gradlew runData` 通过：模组加载、Mixin 注入（`DoorBlockMixin`）、配置生成、事件订阅全部正常
- `gradlew runServer` 通过：服务端正常启动（`Done`），**配方数从原版 1290 增加到 1297**（本模组 7 个配方全部生效），
  没有任何数据包解析错误
- 还没做的只是“进游戏手动点一遍”的交互测试，建议用 `gradlew runClient` 自己试一下

---

## 5. 常见问题

### 5.1 报错 `Could not resolve net.neoforged:neoforge` / `Connection reset` / `Plugin [...] was not found`

原因：**你的网络访问不了 `maven.neoforged.net`**（国内网络经常被重置；Maven Central、Mojang、GitHub 都能连，只有这个域名不行）。

解决办法：挂代理，然后在 `gradle.properties` 里打开代理配置：

```properties
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7890
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7890
```

改完重新执行 `.\gradlew.bat build` 即可（Gradle 需要重启，`.\gradlew.bat --stop` 之后再来一次）。

### 5.2 IDE 里满屏红字（`缺少必需的类路径条目`、`无法解析 net.minecraft.*`）

这是依赖还没下载下来的表现，**只要 `.\gradlew.bat build` 成功一次**，再让 IDE 重新导入 Gradle 项目就正常了。

### 5.3 版本号不对怎么改

统一改 `gradle.properties`：

```properties
neo_version=21.1.235   # 21.1.x 任意可用版本，见 https://maven.neoforged.net/releases/net/neoforged/neoforge/
```

NeoGradle 插件版本在 `build.gradle` 第一行的 `id 'net.neoforged.gradle.userdev' version '7.1.38'`，
当前组合（Gradle 9.2.1 + NeoGradle 7.1.38 + NeoForge 21.1.235）与官方 MDK 完全一致。

### 5.4 数据包目录名是**单数**

Minecraft 1.21 / 1.21.1 的配方、标签目录用的是单数：

```
data/<命名空间>/recipe/          配方
data/<命名空间>/tags/block/      方块标签
data/<命名空间>/tags/item/       物品标签
```

（1.20.x 及更早才是复数 `recipes/`、`tags/blocks/`、`tags/items/`。）
用错目录名不会有任何报错，只是配方会静默不生效。本项目已用单数写法并实测验证过。

### 5.6 构建时提示 `NeoGradle detected a problem with your project: Run.getProgramArguments()`

这是 NeoGradle 7.1.38 自己的提醒（说这个写法以后会改名），当前版本构建完全正常
（`BUILD SUCCESSFUL`），可以直接忽略。

### 5.5 没有代理怎么办？

项目里带了 `.github/workflows/build.yml`：把项目推到 GitHub 后，
Actions 会自动在 GitHub 的机器上构建（那边网络没有限制），
构建完成后到 Actions 页面下载 `locksmith-jar` 产物即可。

---

## 6. 已知限制（v1.0）

- **村民仍然可以打开已上锁的木门**（村民寻路是直接调 `DoorBlock#setOpen`，没有事件可拦截）。
- 多人游戏里开锁瞬间，客户端可能对门做一次“预测打开”，服务端会在同一 tick 把状态推回去，
  所以门可能会闪一下（单机几乎看不到）。
- 活塞把门推掉、或者用 `/setblock` 替换门方块时，锁数据会被留成孤儿数据（不影响正常使用）。
- `DoorBlockMixin` 的注入参数表是按 **1.21.1 的原版签名写死的**（Mixin 要求注入方法必须声明完整参数表，
  少写参数会在启动时抛 `InvalidInjectionException`）。换游戏版本时需要同步修改那个方法签名。

---

## 7. 目录结构

```
src/main/java/org/examplee/locksmith/
├── Locksmith.java              主类（注册物品、数据组件、创造模式物品栏、配置）
├── LocksmithConfig.java        配置项
├── event/LockEvents.java       右键上锁/开锁、破坏门掉落锁（全部在服务端执行）
├── item/LockItem.java          锁物品
├── item/KeyItem.java           钥匙物品
├── lock/LockManager.java       世界存档里的锁数据（SavedData）
├── lock/LockData.java          单把锁的数据
├── lock/KeyData.java           钥匙的数据组件
├── lock/LockHelper.java        判断可锁方块、计算双开门等目标坐标
├── lock/LockTier.java          材质等级
├── mixin/DoorBlockMixin.java   让已上锁的门忽略红石
└── registry/                   物品 / 创造模式物品栏 / 数据组件 / 标签注册

src/main/resources/
├── META-INF/neoforge.mods.toml mod 元数据
├── locksmith.mixins.json       mixin 配置
├── assets/locksmith/           模型、贴图、语言文件（zh_cn / en_us）
└── data/locksmith/             配方、标签

tools/generate_textures.ps1     重新生成物品贴图（改配色用）
```

贴图是用 `tools/generate_textures.ps1` 按像素画生成的，想换配色改脚本里的颜色再跑一次即可。

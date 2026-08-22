# MekaTFC 开发规范与提示词

## TFC (TerraFirmaCraft) 地质与岩石规则

1. **TFC 仅有以下 20 种原生岩石类型**：
   - **侵入火成岩 (Igneous Intrusive)**: 花岗岩 (`granite`)、闪长岩 (`diorite`)、辉长岩 (`gabbro`)
   - **喷出火成岩 (Igneous Extrusive)**: 流纹岩 (`rhyolite`)、玄武岩 (`basalt`)、安山岩 (`andesite`)、英安岩 (`dacite`)
   - **沉积岩 (Sedimentary)**: 页岩 (`shale`)、粘土岩 (`claystone`)、石灰岩 (`limestone`)、砾岩 (`conglomerate`)、白云岩 (`dolomite`)、燧石 (`chert`)、白垩岩 (`chalk`)
   - **变质岩 (Metamorphic)**: 石英岩 (`quartzite`)、板岩 (`slate`)、千枚岩 (`phyllite`)、片岩 (`schist`)、片麻岩 (`gneiss`)、大理岩 (`marble`)

2. **岩石与矿石禁令**：
   - **TFC 内绝对不存在凝灰岩（Tuff）矿石或岩石！** 原版 Minecraft 虽然存在 `minecraft:tuff`，但 TFC 绝无 `tfc:rock/raw/tuff`。
   - **严禁**在任何 TFC 矿脉世界生成（Worldgen）、方块注册、物品注册、模型（Models）、状态（Blockstates）、掉落物表（Loot Tables）、标签（Tags）或配方中包含 `tuff` / 凝灰岩。
   - 编写任何涉及岩石遍历的代码与脚本时，必须严格使用上述 20 种 TFC 原生岩石。

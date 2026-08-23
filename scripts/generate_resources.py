import os
import json
from PIL import Image

BASE_DIR = "src/main/resources"
ASSETS_DIR = os.path.join(BASE_DIR, "assets", "mekatfc")
DATA_DIR = os.path.join(BASE_DIR, "data")

ROCKS = [
    "granite", "diorite", "gabbro", "shale", "claystone", "limestone",
    "conglomerate", "dolomite", "chert", "chalk", "rhyolite",
    "basalt", "andesite", "dacite", "quartzite", "slate", "phyllite",
    "schist", "gneiss", "marble"
]

GRADES = ["poor", "normal", "rich"]

# ==================== 1. 像素贴图绘制 ====================

def create_image_from_grid(grid, palette):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(grid):
        for x, char in enumerate(row):
            if char in palette:
                im.putpixel((x, y), palette[char])
    return im

# ----------------- 方铅矿 (Galena) 调色板 -----------------
PALETTE_GALENA = {
    'S': (38, 44, 52, 255),    # 投影暗色
    'D': (62, 72, 85, 255),    # 暗面铅灰
    'M': (92, 105, 122, 255),  # 基础铅灰
    'L': (142, 156, 175, 255), # 亮面铅灰
    'H': (198, 210, 226, 255), # 立方棱边高光
    'W': (235, 242, 252, 255), # 银星白亮点
}

# ----------------- 沥青铀矿 (Pitchblende) 调色板 -----------------
PALETTE_PITCHBLENDE = {
    'S': (18, 24, 18, 255),    # 边缘暗影
    'B': (28, 36, 26, 255),    # 沥青黑
    'O': (48, 66, 38, 255),    # 暗橄榄绿
    'G': (86, 128, 52, 255),   # 铀绿
    'Y': (142, 196, 58, 255),  # 亮黄绿
    'W': (195, 238, 88, 255),  # 荧光高光
}

# ----------------- 方铅矿 Overlay 形状 (阶梯立方体晶簇) -----------------
GRID_POOR_GALENA_OVERLAY = [
    "................",
    "....HHH.........",
    "...HLLLMH.......",
    "...HLMMDM.......",
    "...HDDDDS.......",
    "....SSSS........",
    "................",
    "................",
    ".........HHH....",
    "........HLLMH...",
    "........HLWDM...",
    "........HDDDDS..",
    ".........SSSS...",
    "................",
    "................",
    "................",
]

GRID_NORMAL_GALENA_OVERLAY = [
    "................",
    "...HHH..........",
    "..HLLLMH...HHH..",
    "..HLMMDM..HLLMH.",
    "..HLWDDM..HLMDM.",
    "..HDDDDSS.HDDDSS",
    "...SSSS.HHH.SS..",
    ".......HLLLMH...",
    ".......HLMMDM...",
    ".......HLWDDM...",
    ".......HDDDDSS..",
    "........SSSS....",
    "...HHH..........",
    "..HLLMH.........",
    "..HDDDSS........",
    "...SSS..........",
]

GRID_RICH_GALENA_OVERLAY = [
    "...HHH..........",
    "..HLLLMH...HHH..",
    "..HLMMDM..HLLLMH",
    "..HLWDDM..HLMMDM",
    "..HDDDDSS.HLWDDM",
    "...SSSS.HHHHMMDS",
    "..HHH..HLLLLLDDS",
    ".HLLMH.HLMMMMMDM",
    ".HLMDM.HLMWDDDDM",
    ".HDDDSSHDDDDDDSS",
    "..SSSS.SSSHHHSS.",
    ".........HLLLMH.",
    "..HHH....HLMMDM.",
    ".HLLLMH..HLWDDM.",
    ".HDDDDSS.HDDDSS.",
    "..SSSS....SSSS..",
]

# ----------------- 沥青铀矿 Overlay 形状 (葡萄状/肾状结核圆斑) -----------------
GRID_POOR_PITCHBLENDE_OVERLAY = [
    "................",
    ".....SS.........",
    "....SBBOS.......",
    "...SBOGGYBS.....",
    "...SOYYWGYBS....",
    "....SBGGYBOS....",
    ".....SOBOS......",
    "......SS........",
    "................",
    "...........SS...",
    "..........SBOS..",
    ".........SBOYYBS",
    ".........SOGWGO.",
    "..........SBOBS.",
    "...........SS...",
    "................",
]

GRID_NORMAL_PITCHBLENDE_OVERLAY = [
    "................",
    "....SS....SS....",
    "...SBOS..SBBOS..",
    "..SBOYYBSBOGGYBS",
    "..SOGWGYSOYYWGYB",
    "...SBGGYBOGGYBOS",
    "....SOBOSBOBOS..",
    ".....SS...SS....",
    "................",
    "......SS........",
    ".....SBBOS..SS..",
    "....SBOGGYBSBOS.",
    "....SOYYWGYOGGYB",
    ".....SBGGYBBOBOS",
    "......SOBOS.SS..",
    ".......SS.......",
]

GRID_RICH_PITCHBLENDE_OVERLAY = [
    "....SS....SS....",
    "...SBOS..SBBOS..",
    "..SBOYYBSBOGGYBS",
    "..SOGWGYSOYYWGYB",
    "...SBGGYBOGGYBOS",
    "....SOBOSBOBOS..",
    "..SS.SS.SBOS..SS",
    ".SBOS..SBOYYBSBB",
    "SBOYYBSOGWGYOGGY",
    "SOGWGYSOBGGYSOYB",
    ".SBGGYBOSOBOSSO.",
    "..SOBOS...SS....",
    "...SS....SS...SS",
    "........SBOS.SBB",
    ".......SBOYYBSOG",
    "........SOBOBS..",
]

# ----------------- 物品原料贴图 -----------------
GRID_POOR_GALENA_ITEM = [
    "................",
    "................",
    "......HHH.......",
    ".....HLLLMH.....",
    "....HLLMMDM.....",
    "....HLMMWDM.....",
    "....HLMDDDM.....",
    "....HDDDDDS.....",
    ".....SSSS.......",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
]

GRID_NORMAL_GALENA_ITEM = [
    "................",
    "......HHH.......",
    ".....HLLLMH.....",
    "....HLLMMDM.HHH.",
    "...HLLMMWDMHLLMH",
    "...HLMMDDDMHLMDM",
    "...HLMDDDDDHDDDM",
    "...HDDDDDDDDDDS.",
    "....SSSDDSSSS...",
    ".....SSSS.......",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
]

GRID_RICH_GALENA_ITEM = [
    "................",
    ".....HHH........",
    "....HLLLMH..HHH.",
    "...HLLMMDM.HLLLM",
    "..HLLMMWDMHLLMMD",
    "..HLMMDDDMHLMWDD",
    "..HLMDDDDDHLMDDD",
    "..HDDDDDDDHDDDDD",
    "...SDDDDDDDDDDSS",
    "....SSDDDDDDSS..",
    "......SSSSSS....",
    "................",
    "................",
    "................",
    "................",
    "................",
]

GRID_SMALL_GALENA_ITEM = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    ".......HH.......",
    "......HLLM......",
    ".....HLMWDM.....",
    ".....HDDDDS.....",
    "......SSSS......",
    "................",
    "................",
    "................",
    "................",
    "................",
]

GRID_POOR_PITCHBLENDE_ITEM = [
    "................",
    "................",
    "......SS........",
    ".....SBBOS......",
    "....SBOGGYBS....",
    "....SOYYWGYBS...",
    "....SBGGYBOS....",
    ".....SOBOS......",
    "......SS........",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
]

GRID_NORMAL_PITCHBLENDE_ITEM = [
    "................",
    ".....SS.........",
    "....SBBOS..SS...",
    "...SBOGGYBSBBOS.",
    "..SBOYYWGYSOYYWB",
    "..SOGWGGYBOGGYBO",
    "...SBGGYBBOBOS..",
    "....SOBOS..SS...",
    ".....SS.........",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
]

GRID_RICH_PITCHBLENDE_ITEM = [
    "................",
    ".....SS...SS....",
    "....SBBOSSBBOS..",
    "...SBOGGYBOGGYBS",
    "..SBOYYWGYSOYYWG",
    "..SOGWGGYBOGGYBO",
    "..SBGGYBBOBOSBOS",
    "...SOBOSBOYYWGYB",
    "....SS.SOGWGGYBO",
    "........SBGGYBOS",
    ".........SOBOS..",
    "..........SS....",
    "................",
    "................",
    "................",
    "................",
]

GRID_SMALL_PITCHBLENDE_ITEM = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "......SS........",
    ".....SBBOS......",
    "....SBOYYBS.....",
    "....SOGWGYBS....",
    ".....SBGGYB.....",
    "......SOBS......",
    ".......SS.......",
    "................",
    "................",
    "................",
    "................",
]

# ----------------- 双锭贴图 -----------------
PALETTE_LEAD_INGOT = {
    'S': (38, 44, 52, 255),
    'D': (60, 70, 82, 255),
    'M': (88, 100, 116, 255),
    'L': (132, 146, 165, 255),
    'H': (185, 196, 212, 255),
}

PALETTE_URANIUM_INGOT = {
    'S': (20, 26, 20, 255),
    'D': (36, 48, 32, 255),
    'M': (58, 80, 46, 255),
    'L': (98, 142, 60, 255),
    'H': (155, 212, 75, 255),
}

GRID_DOUBLE_INGOT = [
    ".........HHH....",
    "......HHHHLLH...",
    "...HHHHLLLLLL...",
    ".HHHLLLLLLLLD...",
    "HLLLLLLLLLLDDDDD",
    "LLLLLLLLLLDDDDDD",
    "LLLLLLLLLDDDDDDD",
    "LLLLLLLLDDDDDDDD",
    "LLLLLLLDDDDDDDDD",
    "LLLLLLDDDDDDDDDD",
    "LLLLLDDDDDDDDDDD",
    "LLLLDDDDDDDDDDDD",
    "LLLDDDDDDDDDDDD.",
    ".LDDDDDDDDDD....",
    "..DDDDDDDD......",
    "...DDDD.........",
]

def save_textures():
    textures = [
        # 方块 Overlay
        (os.path.join(ASSETS_DIR, "textures", "block", "ore", "poor_galena.png"), GRID_POOR_GALENA_OVERLAY, PALETTE_GALENA),
        (os.path.join(ASSETS_DIR, "textures", "block", "ore", "normal_galena.png"), GRID_NORMAL_GALENA_OVERLAY, PALETTE_GALENA),
        (os.path.join(ASSETS_DIR, "textures", "block", "ore", "rich_galena.png"), GRID_RICH_GALENA_OVERLAY, PALETTE_GALENA),
        (os.path.join(ASSETS_DIR, "textures", "block", "ore", "poor_pitchblende.png"), GRID_POOR_PITCHBLENDE_OVERLAY, PALETTE_PITCHBLENDE),
        (os.path.join(ASSETS_DIR, "textures", "block", "ore", "normal_pitchblende.png"), GRID_NORMAL_PITCHBLENDE_OVERLAY, PALETTE_PITCHBLENDE),
        (os.path.join(ASSETS_DIR, "textures", "block", "ore", "rich_pitchblende.png"), GRID_RICH_PITCHBLENDE_OVERLAY, PALETTE_PITCHBLENDE),
        # 物品原料
        (os.path.join(ASSETS_DIR, "textures", "item", "ore", "poor_galena.png"), GRID_POOR_GALENA_ITEM, PALETTE_GALENA),
        (os.path.join(ASSETS_DIR, "textures", "item", "ore", "normal_galena.png"), GRID_NORMAL_GALENA_ITEM, PALETTE_GALENA),
        (os.path.join(ASSETS_DIR, "textures", "item", "ore", "rich_galena.png"), GRID_RICH_GALENA_ITEM, PALETTE_GALENA),
        (os.path.join(ASSETS_DIR, "textures", "item", "ore", "small_galena.png"), GRID_SMALL_GALENA_ITEM, PALETTE_GALENA),
        (os.path.join(ASSETS_DIR, "textures", "item", "ore", "poor_pitchblende.png"), GRID_POOR_PITCHBLENDE_ITEM, PALETTE_PITCHBLENDE),
        (os.path.join(ASSETS_DIR, "textures", "item", "ore", "normal_pitchblende.png"), GRID_NORMAL_PITCHBLENDE_ITEM, PALETTE_PITCHBLENDE),
        (os.path.join(ASSETS_DIR, "textures", "item", "ore", "rich_pitchblende.png"), GRID_RICH_PITCHBLENDE_ITEM, PALETTE_PITCHBLENDE),
        (os.path.join(ASSETS_DIR, "textures", "item", "ore", "small_pitchblende.png"), GRID_SMALL_PITCHBLENDE_ITEM, PALETTE_PITCHBLENDE),
        # 双锭
        (os.path.join(ASSETS_DIR, "textures", "item", "metal", "double_ingot", "lead.png"), GRID_DOUBLE_INGOT, PALETTE_LEAD_INGOT),
        (os.path.join(ASSETS_DIR, "textures", "item", "metal", "double_ingot", "uranium.png"), GRID_DOUBLE_INGOT, PALETTE_URANIUM_INGOT),
    ]

    for path, grid, palette in textures:
        os.makedirs(os.path.dirname(path), exist_ok=True)
        img = create_image_from_grid(grid, palette)
        img.save(path)
        print(f"Generated texture: {path}")

# ==================== 2. 模型与状态生成 ====================

def generate_models_and_blockstates():
    ores = ["galena", "pitchblende"]
    
    for ore in ores:
        # 小地表矿石模型
        groundcover_model = {
            "credit": "Made for MekaTFC",
            "render_type": "minecraft:cutout",
            "textures": {
                "0": f"mekatfc:item/ore/small_{ore}",
                "particle": f"mekatfc:item/ore/small_{ore}"
            },
            "elements": [
                {"from": [5, 0, 7], "to": [10, 1, 9], "faces": {"north": {"uv": [4, 6, 9, 7], "texture": "#0"}, "east": {"uv": [4, 6, 5, 7], "texture": "#0"}, "south": {"uv": [6, 4, 11, 5], "texture": "#0"}, "west": {"uv": [12, 7, 14, 8], "texture": "#0"}, "up": {"uv": [6, 8, 11, 10], "texture": "#0"}, "down": {"uv": [5, 5, 10, 7], "texture": "#0", "cullface": "down"}}},
                {"from": [5, 1, 8], "to": [10, 2, 9], "faces": {"north": {"uv": [6, 7, 11, 8], "texture": "#0"}, "east": {"uv": [6, 8, 7, 9], "texture": "#0"}, "south": {"uv": [6, 6, 11, 7], "texture": "#0"}, "west": {"uv": [7, 5, 8, 6], "texture": "#0"}, "up": {"uv": [5, 9, 10, 10], "rotation": 180, "texture": "#0"}}},
                {"from": [7, 1, 9], "to": [10, 2, 10], "faces": {"east": {"uv": [4, 6, 5, 7], "texture": "#0"}, "south": {"uv": [6, 6, 9, 7], "texture": "#0"}, "west": {"uv": [7, 6, 8, 7], "texture": "#0"}, "up": {"uv": [4, 6, 7, 7], "texture": "#0"}}},
                {"from": [5, 1, 7], "to": [8, 2, 8], "faces": {"north": {"uv": [6, 8, 9, 9], "texture": "#0"}, "east": {"uv": [6, 8, 7, 9], "texture": "#0"}, "west": {"uv": [7, 6, 8, 7], "texture": "#0"}, "up": {"uv": [10, 7, 13, 8], "rotation": 180, "texture": "#0"}}},
                {"from": [7, 0, 9], "to": [10, 1, 10], "faces": {"east": {"uv": [4, 6, 5, 7], "texture": "#0"}, "south": {"uv": [4, 12, 7, 13], "texture": "#0"}, "west": {"uv": [10, 11, 11, 12], "texture": "#0"}, "down": {"uv": [5, 5, 8, 6], "texture": "#0", "cullface": "down"}}},
                {"from": [6, 0, 6], "to": [9, 1, 7], "faces": {"north": {"uv": [5, 6, 8, 7], "texture": "#0"}, "east": {"uv": [4, 6, 5, 7], "texture": "#0"}, "west": {"uv": [6, 6, 7, 7], "texture": "#0"}, "up": {"uv": [7, 4, 10, 5], "texture": "#0"}, "down": {"uv": [5, 5, 8, 6], "texture": "#0", "cullface": "down"}}},
                {"from": [10, 0, 8], "to": [11, 1, 9], "faces": {"north": {"uv": [6, 7, 7, 8], "texture": "#0"}, "east": {"uv": [4, 6, 5, 7], "texture": "#0"}, "south": {"uv": [6, 6, 7, 7], "texture": "#0"}, "up": {"uv": [4, 6, 5, 7], "texture": "#0"}, "down": {"uv": [5, 5, 6, 6], "texture": "#0", "cullface": "down"}}}
            ]
        }
        p_gc_mod = os.path.join(ASSETS_DIR, "models", "block", "groundcover", f"{ore}.json")
        os.makedirs(os.path.dirname(p_gc_mod), exist_ok=True)
        with open(p_gc_mod, "w", encoding="utf-8") as f:
            json.dump(groundcover_model, f, indent=2)

        # 小地表矿石 blockstate
        gc_bs = {
            "variants": {
                "": [
                    {"model": f"mekatfc:block/groundcover/{ore}", "y": 90},
                    {"model": f"mekatfc:block/groundcover/{ore}"},
                    {"model": f"mekatfc:block/groundcover/{ore}", "y": 180},
                    {"model": f"mekatfc:block/groundcover/{ore}", "y": 270}
                ]
            }
        }
        p_gc_bs = os.path.join(ASSETS_DIR, "blockstates", "ore", f"small_{ore}.json")
        os.makedirs(os.path.dirname(p_gc_bs), exist_ok=True)
        with open(p_gc_bs, "w", encoding="utf-8") as f:
            json.dump(gc_bs, f, indent=2)

        # 原料物品模型
        for grade in GRADES:
            item_mod = {
                "parent": "minecraft:item/generated",
                "textures": {
                    "layer0": f"mekatfc:item/ore/{grade}_{ore}"
                }
            }
            p_im = os.path.join(ASSETS_DIR, "models", "item", "ore", f"{grade}_{ore}.json")
            os.makedirs(os.path.dirname(p_im), exist_ok=True)
            with open(p_im, "w", encoding="utf-8") as f:
                json.dump(item_mod, f, indent=2)

        small_item_mod = {
            "parent": "minecraft:item/generated",
            "textures": {
                "layer0": f"mekatfc:item/ore/small_{ore}"
            }
        }
        p_sim = os.path.join(ASSETS_DIR, "models", "item", "ore", f"small_{ore}.json")
        with open(p_sim, "w", encoding="utf-8") as f:
            json.dump(small_item_mod, f, indent=2)

        # 21 种岩石的矿石方块模型、Blockstates、Item Models
        for grade in GRADES:
            for rock in ROCKS:
                # 方块模型
                b_mod = {
                    "parent": "tfc:block/ore",
                    "render_type": "minecraft:cutout_mipped",
                    "textures": {
                        "all": f"tfc:block/rock/raw/{rock}",
                        "overlay": f"mekatfc:block/ore/{grade}_{ore}"
                    }
                }
                p_bm = os.path.join(ASSETS_DIR, "models", "block", "ore", f"{grade}_{ore}", f"{rock}.json")
                os.makedirs(os.path.dirname(p_bm), exist_ok=True)
                with open(p_bm, "w", encoding="utf-8") as f:
                    json.dump(b_mod, f, indent=2)

                # Blockstate
                bs = {
                    "variants": {
                        "": {
                            "model": f"mekatfc:block/ore/{grade}_{ore}/{rock}"
                        }
                    }
                }
                p_bs = os.path.join(ASSETS_DIR, "blockstates", "ore", f"{grade}_{ore}", f"{rock}.json")
                os.makedirs(os.path.dirname(p_bs), exist_ok=True)
                with open(p_bs, "w", encoding="utf-8") as f:
                    json.dump(bs, f, indent=2)

                # BlockItem 模型
                bi_mod = {
                    "parent": f"mekatfc:block/ore/{grade}_{ore}/{rock}"
                }
                p_bim = os.path.join(ASSETS_DIR, "models", "item", "ore", f"{grade}_{ore}", f"{rock}.json")
                os.makedirs(os.path.dirname(p_bim), exist_ok=True)
                with open(p_bim, "w", encoding="utf-8") as f:
                    json.dump(bi_mod, f, indent=2)

    # 双锭模型
    for metal in ["lead", "uranium"]:
        di_mod = {
            "parent": "minecraft:item/generated",
            "textures": {
                "layer0": f"mekatfc:item/metal/double_ingot/{metal}"
            }
        }
        p_dim = os.path.join(ASSETS_DIR, "models", "item", "metal", "double_ingot", f"{metal}.json")
        os.makedirs(os.path.dirname(p_dim), exist_ok=True)
        with open(p_dim, "w", encoding="utf-8") as f:
            json.dump(di_mod, f, indent=2)

        # 桶模型
        bucket_mod = {
            "parent": "minecraft:item/generated",
            "textures": {
                "layer0": "minecraft:item/bucket",
                "layer1": "tfc:item/metal/bucket/flowing"
            }
        }
        p_bm = os.path.join(ASSETS_DIR, "models", "item", "metal", "bucket", f"{metal}.json")
        os.makedirs(os.path.dirname(p_bm), exist_ok=True)
        with open(p_bm, "w", encoding="utf-8") as f:
            json.dump(bucket_mod, f, indent=2)

    print("Generated all models and blockstates.")

# ==================== 3. 掉落物表生成 ====================

def generate_loot_tables():
    ores = ["galena", "pitchblende"]
    for ore in ores:
        # 小地表矿石
        small_lt = {
            "type": "minecraft:block",
            "pools": [
                {
                    "bonus_rolls": 0.0,
                    "entries": [
                        {
                            "type": "minecraft:item",
                            "functions": [
                                {
                                    "function": "minecraft:explosion_decay"
                                }
                            ],
                            "name": f"mekatfc:ore/small_{ore}"
                        }
                    ],
                    "rolls": 1.0
                }
            ]
        }
        p_slt = os.path.join(DATA_DIR, "mekatfc", "loot_table", "blocks", "ore", f"small_{ore}.json")
        os.makedirs(os.path.dirname(p_slt), exist_ok=True)
        with open(p_slt, "w", encoding="utf-8") as f:
            json.dump(small_lt, f, indent=2)

        # 矿石方块
        for grade in GRADES:
            for rock in ROCKS:
                lt = {
                    "type": "minecraft:block",
                    "pools": [
                        {
                            "bonus_rolls": 0.0,
                            "entries": [
                                {
                                    "type": "minecraft:alternatives",
                                    "children": [
                                        {
                                            "type": "minecraft:item",
                                            "conditions": [
                                                {
                                                    "condition": "minecraft:match_tool",
                                                    "predicate": {
                                                        "predicates": {
                                                            "minecraft:enchantments": [
                                                                {
                                                                    "enchantments": "#minecraft:silk_touch",
                                                                    "levels": {"min": 1}
                                                                }
                                                            ]
                                                        }
                                                    }
                                                }
                                            ],
                                            "name": f"mekatfc:ore/{grade}_{ore}/{rock}"
                                        },
                                        {
                                            "type": "minecraft:item",
                                            "functions": [
                                                {
                                                    "enchantment": "minecraft:fortune",
                                                    "formula": "minecraft:ore_drops",
                                                    "function": "minecraft:apply_bonus"
                                                },
                                                {
                                                    "function": "minecraft:explosion_decay"
                                                }
                                            ],
                                            "name": f"mekatfc:ore/{grade}_{ore}"
                                        }
                                    ]
                                }
                            ],
                            "rolls": 1.0
                        }
                    ]
                }
                p_lt = os.path.join(DATA_DIR, "mekatfc", "loot_table", "blocks", "ore", f"{grade}_{ore}", f"{rock}.json")
                os.makedirs(os.path.dirname(p_lt), exist_ok=True)
                with open(p_lt, "w", encoding="utf-8") as f:
                    json.dump(lt, f, indent=2)

    print("Generated all loot tables.")

# ==================== 4. 标签与语言文件更新 ====================

ROCK_NAMES_CN = {
    "granite": "花岗岩", "diorite": "闪长岩", "gabbro": "辉长岩", "shale": "页岩",
    "claystone": "粘土岩", "limestone": "石灰岩", "conglomerate": "砾岩", "dolomite": "白云岩",
    "chert": "燧石", "chalk": "白垩岩", "rhyolite": "流纹岩",
    "basalt": "玄武岩", "andesite": "安山岩", "dacite": "英安岩", "quartzite": "石英岩",
    "slate": "板岩", "phyllite": "千枚岩", "schist": "片岩", "gneiss": "片麻岩", "marble": "大理岩"
}

ROCK_NAMES_EN = {
    "granite": "Granite", "diorite": "Diorite", "gabbro": "Gabbro", "shale": "Shale",
    "claystone": "Claystone", "limestone": "Limestone", "conglomerate": "Conglomerate", "dolomite": "Dolomite",
    "chert": "Chert", "chalk": "Chalk", "rhyolite": "Rhyolite",
    "basalt": "Basalt", "andesite": "Andesite", "dacite": "Dacite", "quartzite": "Quartzite",
    "slate": "Slate", "phyllite": "Phyllite", "schist": "Schist", "gneiss": "Gneiss", "marble": "Marble"
}

GRADE_NAMES_CN = {"poor": "贫瘠", "normal": "", "rich": "富集"}
GRADE_NAMES_EN = {"poor": "Poor ", "normal": "", "rich": "Rich "}

def update_lang_files():
    # zh_cn.json
    zh_path = os.path.join(ASSETS_DIR, "lang", "zh_cn.json")
    with open(zh_path, "r", encoding="utf-8-sig") as f:
        zh_data = json.load(f)

    zh_data["item.mekatfc.metal.double_ingot.lead"] = "铅双锭"
    zh_data["item.mekatfc.metal.double_ingot.uranium"] = "铀双锭"
    zh_data["item.mekatfc.metal.bucket.lead"] = "熔融铅桶"
    zh_data["item.mekatfc.metal.bucket.uranium"] = "熔融铀桶"
    zh_data["fluid.mekatfc.metal.lead"] = "熔融铅"
    zh_data["fluid.mekatfc.metal.uranium"] = "熔融铀"
    zh_data["block.mekatfc.metal.lead"] = "熔融铅"
    zh_data["block.mekatfc.metal.uranium"] = "熔融铀"

    # 方铅矿
    zh_data["item.mekatfc.ore.poor_galena"] = "贫瘠方铅矿"
    zh_data["item.mekatfc.ore.normal_galena"] = "方铅矿"
    zh_data["item.mekatfc.ore.rich_galena"] = "富集方铅矿"
    zh_data["item.mekatfc.ore.small_galena"] = "小方铅矿"
    zh_data["block.mekatfc.ore.small_galena"] = "小方铅矿"
    zh_data["block.mekatfc.ore.small_galena.prospected"] = "方铅矿"

    # 沥青铀矿
    zh_data["item.mekatfc.ore.poor_pitchblende"] = "贫瘠沥青铀矿"
    zh_data["item.mekatfc.ore.normal_pitchblende"] = "沥青铀矿"
    zh_data["item.mekatfc.ore.rich_pitchblende"] = "富集沥青铀矿"
    zh_data["item.mekatfc.ore.small_pitchblende"] = "小沥青铀矿"
    zh_data["block.mekatfc.ore.small_pitchblende"] = "小沥青铀矿"
    zh_data["block.mekatfc.ore.small_pitchblende.prospected"] = "沥青铀矿"

    for rock in ROCKS:
        r_cn = ROCK_NAMES_CN[rock]
        for grade in GRADES:
            g_cn = GRADE_NAMES_CN[grade]
            # galena
            zh_data[f"block.mekatfc.ore.{grade}_galena.{rock}"] = f"{g_cn}{r_cn}方铅矿"
            zh_data[f"block.mekatfc.ore.{grade}_galena.{rock}.prospected"] = "方铅矿"
            # pitchblende
            zh_data[f"block.mekatfc.ore.{grade}_pitchblende.{rock}"] = f"{g_cn}{r_cn}沥青铀矿"
            zh_data[f"block.mekatfc.ore.{grade}_pitchblende.{rock}.prospected"] = "沥青铀矿"

    with open(zh_path, "w", encoding="utf-8") as f:
        json.dump(zh_data, f, ensure_ascii=False, indent=2)

    # en_us.json
    en_path = os.path.join(ASSETS_DIR, "lang", "en_us.json")
    with open(en_path, "r", encoding="utf-8-sig") as f:
        en_data = json.load(f)

    en_data["item.mekatfc.metal.double_ingot.lead"] = "Lead Double Ingot"
    en_data["item.mekatfc.metal.double_ingot.uranium"] = "Uranium Double Ingot"
    en_data["item.mekatfc.metal.bucket.lead"] = "Molten Lead Bucket"
    en_data["item.mekatfc.metal.bucket.uranium"] = "Molten Uranium Bucket"
    en_data["fluid.mekatfc.metal.lead"] = "Molten Lead"
    en_data["fluid.mekatfc.metal.uranium"] = "Molten Uranium"
    en_data["block.mekatfc.metal.lead"] = "Molten Lead"
    en_data["block.mekatfc.metal.uranium"] = "Molten Uranium"

    # galena
    en_data["item.mekatfc.ore.poor_galena"] = "Poor Galena"
    en_data["item.mekatfc.ore.normal_galena"] = "Galena"
    en_data["item.mekatfc.ore.rich_galena"] = "Rich Galena"
    en_data["item.mekatfc.ore.small_galena"] = "Small Galena"
    en_data["block.mekatfc.ore.small_galena"] = "Small Galena"
    en_data["block.mekatfc.ore.small_galena.prospected"] = "Galena"

    # pitchblende
    en_data["item.mekatfc.ore.poor_pitchblende"] = "Poor Pitchblende"
    en_data["item.mekatfc.ore.normal_pitchblende"] = "Pitchblende"
    en_data["item.mekatfc.ore.rich_pitchblende"] = "Rich Pitchblende"
    en_data["item.mekatfc.ore.small_pitchblende"] = "Small Pitchblende"
    en_data["block.mekatfc.ore.small_pitchblende"] = "Small Pitchblende"
    en_data["block.mekatfc.ore.small_pitchblende.prospected"] = "Pitchblende"

    for rock in ROCKS:
        r_en = ROCK_NAMES_EN[rock]
        for grade in GRADES:
            g_en = GRADE_NAMES_EN[grade]
            # galena
            en_data[f"block.mekatfc.ore.{grade}_galena.{rock}"] = f"{g_en}{r_en} Galena"
            en_data[f"block.mekatfc.ore.{grade}_galena.{rock}.prospected"] = "Galena"
            # pitchblende
            en_data[f"block.mekatfc.ore.{grade}_pitchblende.{rock}"] = f"{g_en}{r_en} Pitchblende"
            en_data[f"block.mekatfc.ore.{grade}_pitchblende.{rock}.prospected"] = "Pitchblende"

    with open(en_path, "w", encoding="utf-8") as f:
        json.dump(en_data, f, ensure_ascii=False, indent=2)

    print("Updated language files.")

def update_tags():
    # forge:tags/blocks/ores/lead.json
    lead_ores = []
    for rock in ROCKS:
        for grade in GRADES:
            lead_ores.append(f"mekatfc:ore/{grade}_galena/{rock}")
    lead_ores.extend([
        {"id": "mekanism:lead_ore", "required": False},
        {"id": "mekanism:deepslate_lead_ore", "required": False}
    ])
    p_lo = os.path.join(DATA_DIR, "forge", "tags", "blocks", "ores", "lead.json")
    os.makedirs(os.path.dirname(p_lo), exist_ok=True)
    with open(p_lo, "w", encoding="utf-8") as f:
        json.dump({"replace": False, "values": lead_ores}, f, indent=2)

    # forge:tags/blocks/ores/uranium.json
    uranium_ores = []
    for rock in ROCKS:
        for grade in GRADES:
            uranium_ores.append(f"mekatfc:ore/{grade}_pitchblende/{rock}")
    uranium_ores.extend([
        {"id": "mekanism:uranium_ore", "required": False},
        {"id": "mekanism:deepslate_uranium_ore", "required": False}
    ])
    p_uo = os.path.join(DATA_DIR, "forge", "tags", "blocks", "ores", "uranium.json")
    os.makedirs(os.path.dirname(p_uo), exist_ok=True)
    with open(p_uo, "w", encoding="utf-8") as f:
        json.dump({"replace": False, "values": uranium_ores}, f, indent=2)

    # tfc:tags/blocks/prospectable.json
    p_prospect = os.path.join(DATA_DIR, "tfc", "tags", "blocks", "prospectable.json")
    os.makedirs(os.path.dirname(p_prospect), exist_ok=True)
    all_prospectable = []
    for ore in ["native_osmium", "galena", "pitchblende"]:
        for rock in ROCKS:
            for grade in GRADES:
                all_prospectable.append(f"mekatfc:ore/{grade}_{ore}/{rock}")
    with open(p_prospect, "w", encoding="utf-8") as f:
        json.dump({"replace": False, "values": all_prospectable}, f, indent=2)

    print("Updated block ore tags and prospectable tag.")

if __name__ == "__main__":
    save_textures()
    generate_models_and_blockstates()
    generate_loot_tables()
    update_lang_files()
    update_tags()
    print("All tasks completed successfully!")

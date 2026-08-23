import os
import json

BASE_DIR = "src/main/resources"
DATA_DIR = os.path.join(BASE_DIR, "data")

ROCKS = [
    "granite", "diorite", "gabbro", "shale", "claystone", "limestone",
    "conglomerate", "dolomite", "chert", "chalk", "rhyolite",
    "basalt", "andesite", "dacite", "quartzite", "slate", "phyllite",
    "schist", "gneiss", "marble"
]

GRADES = ["poor", "normal", "rich"]

# ==================== 1. 世界生成特征 (Worldgen Features) ====================

def generate_worldgen():
    # ----------------- 方铅矿 (Galena - Lead) 脉矿配置 -----------------
    # 方铅矿在地质上广泛赋存于岩浆岩与沉积岩、变质岩中
    galena_rocks = [
        "granite", "diorite", "gabbro", "rhyolite", "andesite",
        "limestone", "dolomite", "claystone", "quartzite", "slate", "phyllite", "schist", "gneiss"
    ]
    galena_blocks = []
    for rock in galena_rocks:
        galena_blocks.append({
            "replace": [f"tfc:rock/raw/{rock}"],
            "with": [
                {"weight": 35, "block": f"mekatfc:ore/poor_galena/{rock}"},
                {"weight": 40, "block": f"mekatfc:ore/normal_galena/{rock}"},
                {"weight": 25, "block": f"mekatfc:ore/rich_galena/{rock}"}
            ]
        })

    galena_configured = {
        "type": "tfc:cluster_vein",
        "config": {
            "rarity": 45,
            "density": 0.50,
            "min_y": -40,
            "max_y": 80,
            "size": 28,
            "random_name": "galena",
            "blocks": galena_blocks,
            "indicator": {
                "rarity": 12,
                "depth": 35,
                "underground_rarity": 1,
                "underground_count": 0,
                "blocks": [
                    {"block": "mekatfc:ore/small_galena"}
                ]
            }
        }
    }
    p_gc = os.path.join(DATA_DIR, "mekatfc", "worldgen", "configured_feature", "vein", "galena.json")
    os.makedirs(os.path.dirname(p_gc), exist_ok=True)
    with open(p_gc, "w", encoding="utf-8") as f:
        json.dump(galena_configured, f, indent=2)

    galena_placed = {
        "feature": "mekatfc:vein/galena",
        "placement": []
    }
    p_gp = os.path.join(DATA_DIR, "mekatfc", "worldgen", "placed_feature", "vein", "galena.json")
    os.makedirs(os.path.dirname(p_gp), exist_ok=True)
    with open(p_gp, "w", encoding="utf-8") as f:
        json.dump(galena_placed, f, indent=2)

    # ----------------- 沥青铀矿 (Pitchblende - Uranium) 脉矿配置 -----------------
    # 沥青铀矿赋存于深层花岗伟晶岩与热液变质岩中
    pitchblende_rocks = [
        "granite", "gabbro", "rhyolite", "dacite", "basalt", "gneiss", "phyllite", "quartzite"
    ]
    pitchblende_blocks = []
    for rock in pitchblende_rocks:
        pitchblende_blocks.append({
            "replace": [f"tfc:rock/raw/{rock}"],
            "with": [
                {"weight": 35, "block": f"mekatfc:ore/poor_pitchblende/{rock}"},
                {"weight": 40, "block": f"mekatfc:ore/normal_pitchblende/{rock}"},
                {"weight": 25, "block": f"mekatfc:ore/rich_pitchblende/{rock}"}
            ]
        })

    pitchblende_configured = {
        "type": "tfc:cluster_vein",
        "config": {
            "rarity": 65,
            "density": 0.40,
            "min_y": -60,
            "max_y": 20,
            "size": 24,
            "random_name": "pitchblende",
            "blocks": pitchblende_blocks,
            "indicator": {
                "rarity": 18,
                "depth": 40,
                "underground_rarity": 1,
                "underground_count": 0,
                "blocks": [
                    {"block": "mekatfc:ore/small_pitchblende"}
                ]
            }
        }
    }
    p_pc = os.path.join(DATA_DIR, "mekatfc", "worldgen", "configured_feature", "vein", "pitchblende.json")
    os.makedirs(os.path.dirname(p_pc), exist_ok=True)
    with open(p_pc, "w", encoding="utf-8") as f:
        json.dump(pitchblende_configured, f, indent=2)

    pitchblende_placed = {
        "feature": "mekatfc:vein/pitchblende",
        "placement": []
    }
    p_pp = os.path.join(DATA_DIR, "mekatfc", "worldgen", "placed_feature", "vein", "pitchblende.json")
    os.makedirs(os.path.dirname(p_pp), exist_ok=True)
    with open(p_pp, "w", encoding="utf-8") as f:
        json.dump(pitchblende_placed, f, indent=2)

    # ----------------- 注册到 TFC 脉矿标签 -----------------
    p_veins_tag = os.path.join(DATA_DIR, "tfc", "tags", "worldgen", "placed_feature", "in_biome", "veins.json")
    with open(p_veins_tag, "r", encoding="utf-8-sig") as f:
        veins_tag = json.load(f)
    
    current_values = veins_tag.get("values", [])
    for v in ["mekatfc:vein/osmium", "mekatfc:vein/galena", "mekatfc:vein/pitchblende"]:
        if v not in current_values:
            current_values.append(v)
    veins_tag["values"] = current_values
    with open(p_veins_tag, "w", encoding="utf-8") as f:
        json.dump(veins_tag, f, indent=2)

    print("Generated all worldgen configured and placed features.")

# ==================== 2. TFC 熔融/加热配方 (Heating Recipes) ====================

def generate_heating_recipes():
    # 铅 (327.0°C) 与 铀 (1132.0°C)
    metals_info = [
        {"metal": "lead", "ore": "galena", "temp": 327.0},
        {"metal": "uranium", "ore": "pitchblende", "temp": 1132.0}
    ]

    for info in metals_info:
        metal = info["metal"]
        ore = info["ore"]
        temp = info["temp"]
        fluid_id = f"mekatfc:metal/{metal}"

        # 1. 矿石加热 (Ore Heating)
        amounts = {"poor": 10, "normal": 25, "rich": 35}
        for grade, amt in amounts.items():
            ore_heat = {
                "type": "tfc:heating",
                "ingredient": {"item": f"mekatfc:ore/{grade}_{ore}"},
                "result_fluid": {"amount": amt, "id": fluid_id},
                "temperature": temp
            }
            p_oh = os.path.join(DATA_DIR, "tfc", "recipes", "heating", "ore", f"{grade}_{ore}.json")
            os.makedirs(os.path.dirname(p_oh), exist_ok=True)
            with open(p_oh, "w", encoding="utf-8") as f:
                json.dump(ore_heat, f, indent=2)

        # 小矿石加热
        small_ore_heat = {
            "type": "tfc:heating",
            "ingredient": {"item": f"mekatfc:ore/small_{ore}"},
            "result_fluid": {"amount": 10, "id": fluid_id},
            "temperature": temp
        }
        p_soh = os.path.join(DATA_DIR, "tfc", "recipes", "heating", "ore", f"small_{ore}.json")
        os.makedirs(os.path.dirname(p_soh), exist_ok=True)
        with open(p_soh, "w", encoding="utf-8") as f:
            json.dump(small_ore_heat, f, indent=2)

        # 2. 金属制品加热 (Ingot, Double Ingot, Nugget, Dust, Raw Material)
        # 锭 (100mb)
        ingot_heat = {
            "type": "tfc:heating",
            "ingredient": {"tag": f"c:ingots/{metal}"},
            "result_fluid": {"amount": 100, "id": fluid_id},
            "temperature": temp
        }
        p_ih = os.path.join(DATA_DIR, "tfc", "recipes", "heating", "metal", "ingot", f"{metal}.json")
        os.makedirs(os.path.dirname(p_ih), exist_ok=True)
        with open(p_ih, "w", encoding="utf-8") as f:
            json.dump(ingot_heat, f, indent=2)

        # 双锭 (200mb)
        double_ingot_heat = {
            "type": "tfc:heating",
            "ingredient": {"tag": f"c:double_ingots/{metal}"},
            "result_fluid": {"amount": 200, "id": fluid_id},
            "temperature": temp
        }
        p_dih = os.path.join(DATA_DIR, "tfc", "recipes", "heating", "metal", "double_ingot", f"{metal}.json")
        os.makedirs(os.path.dirname(p_dih), exist_ok=True)
        with open(p_dih, "w", encoding="utf-8") as f:
            json.dump(double_ingot_heat, f, indent=2)

        # 粒 (10mb)
        nugget_heat = {
            "type": "tfc:heating",
            "ingredient": {"tag": f"c:nuggets/{metal}"},
            "result_fluid": {"amount": 10, "id": fluid_id},
            "temperature": temp
        }
        p_nh = os.path.join(DATA_DIR, "tfc", "recipes", "heating", "metal", "nugget", f"{metal}.json")
        os.makedirs(os.path.dirname(p_nh), exist_ok=True)
        with open(p_nh, "w", encoding="utf-8") as f:
            json.dump(nugget_heat, f, indent=2)

        # 粉 (100mb)
        dust_heat = {
            "type": "tfc:heating",
            "ingredient": {"tag": f"c:dusts/{metal}"},
            "result_fluid": {"amount": 100, "id": fluid_id},
            "temperature": temp
        }
        p_dh = os.path.join(DATA_DIR, "tfc", "recipes", "heating", "metal", "dust", f"{metal}.json")
        os.makedirs(os.path.dirname(p_dh), exist_ok=True)
        with open(p_dh, "w", encoding="utf-8") as f:
            json.dump(dust_heat, f, indent=2)

        # 粗矿 (100mb)
        raw_heat = {
            "type": "tfc:heating",
            "ingredient": {"tag": f"c:raw_materials/{metal}"},
            "result_fluid": {"amount": 100, "id": fluid_id},
            "temperature": temp
        }
        p_rh = os.path.join(DATA_DIR, "tfc", "recipes", "heating", "metal", "raw", f"{metal}.json")
        os.makedirs(os.path.dirname(p_rh), exist_ok=True)
        with open(p_rh, "w", encoding="utf-8") as f:
            json.dump(raw_heat, f, indent=2)

    print("Generated all heating recipes.")

# ==================== 3. TFC 浇铸与焊接配方 (Casting & Welding) ====================

def generate_casting_and_welding():
    for metal, tier in [("lead", 1), ("uranium", 3)]:
        # 陶瓷模具浇铸
        cast_ingot = {
            "type": "tfc:casting",
            "break_chance": 0.1,
            "fluid": {
                "amount": 100,
                "fluid": f"mekatfc:metal/{metal}"
            },
            "mold": {
                "item": "tfc:ceramic/ingot_mold"
            },
            "result": {
                "count": 1,
                "id": f"mekanism:ingot_{metal}"
            }
        }
        p_ci = os.path.join(DATA_DIR, "tfc", "recipes", "casting", f"{metal}_ingot.json")
        os.makedirs(os.path.dirname(p_ci), exist_ok=True)
        with open(p_ci, "w", encoding="utf-8") as f:
            json.dump(cast_ingot, f, indent=2)

        # 耐火陶瓷模具浇铸
        cast_fire_ingot = {
            "type": "tfc:casting",
            "break_chance": 0.01,
            "fluid": {
                "amount": 100,
                "fluid": f"mekatfc:metal/{metal}"
            },
            "mold": {
                "item": "tfc:ceramic/fire_ingot_mold"
            },
            "result": {
                "count": 1,
                "id": f"mekanism:ingot_{metal}"
            }
        }
        p_cfi = os.path.join(DATA_DIR, "tfc", "recipes", "casting", f"fire_ingot_{metal}.json")
        os.makedirs(os.path.dirname(p_cfi), exist_ok=True)
        with open(p_cfi, "w", encoding="utf-8") as f:
            json.dump(cast_fire_ingot, f, indent=2)

        # 铁砧焊接双锭
        welding_recipe = {
            "type": "tfc:welding",
            "first_input": {
                "tag": f"c:ingots/{metal}"
            },
            "second_input": {
                "tag": f"c:ingots/{metal}"
            },
            "result": {
                "count": 1,
                "id": f"mekatfc:metal/double_ingot/{metal}"
            },
            "tier": tier
        }
        p_w = os.path.join(DATA_DIR, "tfc", "recipes", "welding", f"{metal}_double_ingot.json")
        os.makedirs(os.path.dirname(p_w), exist_ok=True)
        with open(p_w, "w", encoding="utf-8") as f:
            json.dump(welding_recipe, f, indent=2)

    print("Generated all casting and welding recipes.")

# ==================== 4. Mekanism 矿物处理流水线配方 ====================

def generate_mekanism_ore_processing():
    """
    为 MekaTFC 矿物（原生锇矿、方铅矿、沥青铀矿）以及 TFC 原生矿物（赤铁矿、磁铁矿、褐铁矿、原生铜、孔雀石、黝铜矿、原生金、锡石）
    生成全套 Mekanism 矿物处理链配方。
    各品级投入量：
      - 贫瘠 (poor) & 小型 (small): 3x
      - 普通 (normal): 2x
      - 富集 (rich): 1x
    标准产出：
      - 富集仓 (Enriching): 4x 粉 (dust)
      - 净化仓 (Purifying): 6x 碎块 (clump) + 1 氧气
      - 化学灌注机 (Injecting): 8x 碎片 (shard) + 1 氯化氢
      - 化学溶解室 (Dissolution): 2000mB 脏矿浆 (dirty slurry) + 100 硫酸
      - 粉碎机 (Crushing): 2x 粉 (dust)
      - 熔炉 (Smelting): poor/small -> 4x 粒, normal -> 1x 锭, rich -> 2x 锭
      - 高炉 (Blasting): poor/small -> 4x 粒, normal -> 1x 锭, rich -> 2x 锭
    """
    materials = [
        # MekaTFC 新增矿物
        {"mod": "mekatfc", "ore": "native_osmium", "metal": "osmium", "is_vanilla": False},
        {"mod": "mekatfc", "ore": "galena", "metal": "lead", "is_vanilla": False},
        {"mod": "mekatfc", "ore": "pitchblende", "metal": "uranium", "is_vanilla": False},
        # TFC 原生矿物 (铁系)
        {"mod": "tfc", "ore": "hematite", "metal": "iron", "is_vanilla": True},
        {"mod": "tfc", "ore": "magnetite", "metal": "iron", "is_vanilla": True},
        {"mod": "tfc", "ore": "limonite", "metal": "iron", "is_vanilla": True},
        # TFC 原生矿物 (铜系)
        {"mod": "tfc", "ore": "native_copper", "metal": "copper", "is_vanilla": True},
        {"mod": "tfc", "ore": "malachite", "metal": "copper", "is_vanilla": True},
        {"mod": "tfc", "ore": "tetrahedrite", "metal": "copper", "is_vanilla": True},
        # TFC 原生矿物 (金系)
        {"mod": "tfc", "ore": "native_gold", "metal": "gold", "is_vanilla": True},
        # TFC 原生矿物 (锡系)
        {"mod": "tfc", "ore": "cassiterite", "metal": "tin", "is_vanilla": False},
    ]

    grade_inputs = {
        "poor": 3,
        "normal": 2,
        "rich": 1,
        "small": 3
    }

    def get_ingot_id(metal, is_vanilla):
        if is_vanilla:
            return f"minecraft:{metal}_ingot"
        return f"mekanism:ingot_{metal}"

    def get_nugget_id(metal, is_vanilla):
        if metal in ["iron", "gold"]:
            return f"minecraft:{metal}_nugget"
        return f"mekanism:nugget_{metal}"

    for mat in materials:
        mod = mat["mod"]
        ore = mat["ore"]
        metal = mat["metal"]
        is_vanilla = mat["is_vanilla"]

        for grade, in_amount in grade_inputs.items():
            item_id = f"{mod}:ore/{grade}_{ore}"
            file_name = f"{grade}_{ore}.json"

            # 1. 富集仓 (Enriching -> 4x dust)
            enrich_recipe = {
                "type": "mekanism:enriching",
                "input": {
                    "amount": in_amount,
                    "ingredient": {
                        "item": item_id
                    }
                },
                "output": {
                    "count": 4,
                    "item": f"mekanism:dust_{metal}"
                }
            }
            p_enrich = os.path.join(DATA_DIR, "mekatfc", "recipes", "enriching", "ore", file_name)
            os.makedirs(os.path.dirname(p_enrich), exist_ok=True)
            with open(p_enrich, "w", encoding="utf-8") as f:
                json.dump(enrich_recipe, f, indent=2)

            # 2. 净化仓 (Purifying -> 6x clump)
            purify_recipe = {
                "type": "mekanism:purifying",
                "itemInput": {
                    "amount": in_amount,
                    "ingredient": {
                        "item": item_id
                    }
                },
                "chemicalInput": {
                    "amount": 1,
                    "gas": "mekanism:oxygen"
                },
                "output": {
                    "count": 6,
                    "item": f"mekanism:clump_{metal}"
                }
            }
            p_purify = os.path.join(DATA_DIR, "mekatfc", "recipes", "purifying", "ore", file_name)
            os.makedirs(os.path.dirname(p_purify), exist_ok=True)
            with open(p_purify, "w", encoding="utf-8") as f:
                json.dump(purify_recipe, f, indent=2)

            # 3. 化学灌注机 (Injecting -> 8x shard)
            inject_recipe = {
                "type": "mekanism:injecting",
                "itemInput": {
                    "amount": in_amount,
                    "ingredient": {
                        "item": item_id
                    }
                },
                "chemicalInput": {
                    "amount": 1,
                    "gas": "mekanism:hydrogen_chloride"
                },
                "output": {
                    "count": 8,
                    "item": f"mekanism:shard_{metal}"
                }
            }
            p_inject = os.path.join(DATA_DIR, "mekatfc", "recipes", "injecting", "ore", file_name)
            os.makedirs(os.path.dirname(p_inject), exist_ok=True)
            with open(p_inject, "w", encoding="utf-8") as f:
                json.dump(inject_recipe, f, indent=2)

            # 4. 化学溶解室 (Dissolution -> 2000mB dirty slurry)
            dissolve_recipe = {
                "type": "mekanism:dissolution",
                "itemInput": {
                    "amount": in_amount,
                    "ingredient": {
                        "item": item_id
                    }
                },
                "gasInput": {
                    "amount": 100,
                    "gas": "mekanism:sulfuric_acid"
                },
                "output": {
                    "amount": 2000,
                    "chemicalType": "slurry",
                    "slurry": f"mekanism:dirty_{metal}"
                }
            }
            p_dissolve = os.path.join(DATA_DIR, "mekatfc", "recipes", "dissolution", "ore", file_name)
            os.makedirs(os.path.dirname(p_dissolve), exist_ok=True)
            with open(p_dissolve, "w", encoding="utf-8") as f:
                json.dump(dissolve_recipe, f, indent=2)

            # 5. 粉碎机 (Crushing -> 2x dust)
            crush_recipe = {
                "type": "mekanism:crushing",
                "input": {
                    "amount": in_amount,
                    "ingredient": {
                        "item": item_id
                    }
                },
                "output": {
                    "count": 2,
                    "item": f"mekanism:dust_{metal}"
                }
            }
            p_crush = os.path.join(DATA_DIR, "mekatfc", "recipes", "crushing", "ore", file_name)
            os.makedirs(os.path.dirname(p_crush), exist_ok=True)
            with open(p_crush, "w", encoding="utf-8") as f:
                json.dump(crush_recipe, f, indent=2)

            # 6. 原版熔炉 (Smelting)
            smelt_result = (
                {"count": 4, "item": get_nugget_id(metal, is_vanilla)} if grade in ["poor", "small"]
                else {"count": 1, "item": get_ingot_id(metal, is_vanilla)} if grade == "normal"
                else {"count": 2, "item": get_ingot_id(metal, is_vanilla)}
            )
            smelt_recipe = {
                "type": "minecraft:smelting",
                "cookingtime": 200,
                "experience": 0.3 if grade in ["poor", "small"] else 0.7 if grade == "normal" else 1.0,
                "ingredient": {
                    "item": item_id
                },
                "result": smelt_result
            }
            p_smelt = os.path.join(DATA_DIR, "mekatfc", "recipes", "smelting", "ore", file_name)
            os.makedirs(os.path.dirname(p_smelt), exist_ok=True)
            with open(p_smelt, "w", encoding="utf-8") as f:
                json.dump(smelt_recipe, f, indent=2)

            # 7. 原版高炉 (Blasting)
            blast_recipe = {
                "type": "minecraft:blasting",
                "cookingtime": 100,
                "experience": 0.3 if grade in ["poor", "small"] else 0.7 if grade == "normal" else 1.0,
                "ingredient": {
                    "item": item_id
                },
                "result": smelt_result
            }
            p_blast = os.path.join(DATA_DIR, "mekatfc", "recipes", "blasting", "ore", file_name)
            os.makedirs(os.path.dirname(p_blast), exist_ok=True)
            with open(p_blast, "w", encoding="utf-8") as f:
                json.dump(blast_recipe, f, indent=2)

    print(f"Generated all Mekanism ore processing recipes for {len(materials)} ores ({len(materials) * 4 * 7} recipe files).")

# ==================== 5. 标签补充 (Tags Updating) ====================

def update_all_tags():
    # 1. tfc:tags/items/ore_pieces.json
    p_op = os.path.join(DATA_DIR, "tfc", "tags", "items", "ore_pieces.json")
    os.makedirs(os.path.dirname(p_op), exist_ok=True)
    all_pieces = []
    for ore in ["native_osmium", "galena", "pitchblende"]:
        for grade in ["poor", "normal", "rich", "small"]:
            all_pieces.append(f"mekatfc:ore/{grade}_{ore}")
    with open(p_op, "w", encoding="utf-8") as f:
        json.dump({"replace": False, "values": all_pieces}, f, indent=2)

    # 2. forge:tags/items/double_ingots.json
    p_di = os.path.join(DATA_DIR, "forge", "tags", "items", "double_ingots.json")
    os.makedirs(os.path.dirname(p_di), exist_ok=True)
    with open(p_di, "w", encoding="utf-8") as f:
        json.dump({"replace": False, "values": [
            "#forge:double_ingots/osmium",
            "#forge:double_ingots/lead",
            "#forge:double_ingots/uranium"
        ]}, f, indent=2)

    # 3. forge:tags/items/double_ingots/{metal}.json
    for metal in ["osmium", "lead", "uranium"]:
        p_dim = os.path.join(DATA_DIR, "forge", "tags", "items", "double_ingots", f"{metal}.json")
        os.makedirs(os.path.dirname(p_dim), exist_ok=True)
        with open(p_dim, "w", encoding="utf-8") as f:
            json.dump({"replace": False, "values": [f"mekatfc:metal/double_ingot/{metal}"]}, f, indent=2)

    # 4. tfc:tags/items/double_ingots.json
    p_tdi = os.path.join(DATA_DIR, "tfc", "tags", "items", "double_ingots.json")
    os.makedirs(os.path.dirname(p_tdi), exist_ok=True)
    with open(p_tdi, "w", encoding="utf-8") as f:
        json.dump({"replace": False, "values": [
            "#forge:double_ingots/osmium",
            "#forge:double_ingots/lead",
            "#forge:double_ingots/uranium"
        ]}, f, indent=2)

    # 5. tfc:tags/fluids/molten_metals.json & metals.json
    fluids = [
        "mekatfc:metal/osmium", "mekatfc:metal/flowing_osmium",
        "mekatfc:metal/lead", "mekatfc:metal/flowing_lead",
        "mekatfc:metal/uranium", "mekatfc:metal/flowing_uranium"
    ]
    p_mm = os.path.join(DATA_DIR, "tfc", "tags", "fluids", "molten_metals.json")
    os.makedirs(os.path.dirname(p_mm), exist_ok=True)
    with open(p_mm, "w", encoding="utf-8") as f:
        json.dump({"replace": False, "values": fluids}, f, indent=2)

    p_fm = os.path.join(DATA_DIR, "tfc", "tags", "fluids", "metals.json")
    os.makedirs(os.path.dirname(p_fm), exist_ok=True)
    with open(p_fm, "w", encoding="utf-8") as f:
        json.dump({"replace": False, "values": fluids}, f, indent=2)

    # 6. forge:tags/fluids/{metal}.json & molten_{metal}.json
    for metal in ["osmium", "lead", "uranium"]:
        p_cf = os.path.join(DATA_DIR, "forge", "tags", "fluids", f"{metal}.json")
        os.makedirs(os.path.dirname(p_cf), exist_ok=True)
        with open(p_cf, "w", encoding="utf-8") as f:
            json.dump({"replace": False, "values": [f"mekatfc:metal/{metal}", f"mekatfc:metal/flowing_{metal}"]}, f, indent=2)

        p_cmf = os.path.join(DATA_DIR, "forge", "tags", "fluids", f"molten_{metal}.json")
        os.makedirs(os.path.dirname(p_cmf), exist_ok=True)
        with open(p_cmf, "w", encoding="utf-8") as f:
            json.dump({"replace": False, "values": [f"mekatfc:metal/{metal}", f"mekatfc:metal/flowing_{metal}"]}, f, indent=2)

    # 7. forge:tags/items/ores/lead.json & uranium.json
    for ore, metal in [("galena", "lead"), ("pitchblende", "uranium")]:
        ore_items = []
        for rock in ROCKS:
            for grade in GRADES:
                ore_items.append(f"mekatfc:ore/{grade}_{ore}/{rock}")
        ore_items.extend([
            {"id": f"mekanism:{metal}_ore", "required": False},
            {"id": f"mekanism:deepslate_{metal}_ore", "required": False}
        ])
        p_oi = os.path.join(DATA_DIR, "forge", "tags", "items", "ores", f"{metal}.json")
        os.makedirs(os.path.dirname(p_oi), exist_ok=True)
        with open(p_oi, "w", encoding="utf-8") as f:
            json.dump({"replace": False, "values": ore_items}, f, indent=2)

    print("Updated all tag files successfully.")

if __name__ == "__main__":
    generate_worldgen()
    generate_heating_recipes()
    generate_casting_and_welding()
    generate_mekanism_ore_processing()
    update_all_tags()
    print("Worldgen, recipes and tags setup finished successfully!")

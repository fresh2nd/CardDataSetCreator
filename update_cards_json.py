
import json
import os

# List of all folders from the previous tool output
card_folders = [
    "OGN-045-Defy", "OGN-169-Gust", "OGN-XXX-Buff", "SFD-T03-Gold", "OGN-043-Charm",
    "OGN-057-Block", "OGS-011-Flash", "OGN-004-Cleave", "OGN-146-Wallop", "OGN-172-Rebuke",
    "OGN-274-Sprite", "OGN-095-Stupefy", "OGN-104-Retreat", "OGN-224-Salvage",
    "OGN-273-Recruit", "OGS-018-Tibbers", "OGN-021-Sun_Disc", "OGN-046-En_Garde",
    "OGN-091-Pit_Crew", "OGN-129-Confront", "OGN-134-Mobilize", "OGN-152-Mistfall",
    "OGN-156-Sabotage", "OGN-256-Fox-Fire", "SFD-161-BF_Sword", "OGN-007-Fury_Rune",
    "OGN-033-Shakedown", "OGN-042-Calm_Rune", "OGN-062-Reinforce", "OGN-064-Wind_Wall",
    "OGN-088-Mega-Mech", "OGN-089-Mind_Rune", "OGN-122-Time_Warp", "OGN-124-Arena_Bar",
    "OGN-126-Body_Rune", "OGN-128-Challenge", "OGN-135-Pakaa_Cub", "OGN-174-Sai_Scout",
    "OGN-182-Scrapheap", "OGN-184-The_Syren", "OGN-187-Whirlwind", "OGN-229-Vengeance",
    "OGN-296-Void_Gate", "OGS-002-Firestorm", "SFD-126-Loyal_Pup", "SFD-188-Void_Rush",
    "OGN-011-Magma_Wurm", "OGN-013-Pouty_Poro", "OGN-025-Blind_Fury", "OGN-048-Meditation",
    "OGN-056-Adaptatron", "OGN-058-Discipline", "OGN-069-Last_Stand", "OGN-131-Dune_Drake",
    "OGN-132-First_Mate", "OGN-136-Pit_Rookie", "OGN-166-Chaos_Rune", "OGN-167-Ember_Monk",
    "OGN-196-Soulgorger", "OGN-199-Tideturner", "OGN-202-Jinx_Rebel", "OGN-203-Possession",
    "OGN-214-Order_Rune", "OGN-271-Recruit_v2", "OGN-272-Recruit_v3", "OGS-003-Incinerate",
    "OGS-020-Highlander", "SFD-007-Gem_Jammer", "SFD-010-Void_Drone", "SFD-022-Long_Sword",
    "SFD-103-Jaull-Fish", "SFD-118-Boneshiver", "SFD-138-Windsinger", "SFD-145-Switcheroo",
    "SFD-150-Last_Rites", "OGN-008-Get_Excited", "OGN-009-Hextech_Ray", "OGN-019-Raging_Soul",
    "OGN-022-Thermo_Beam", "OGN-024-Void_Seeker", "OGN-050-Rune_Prison", "OGN-061-Poro_Herder",
    "OGN-092-Riptide_Rex", "OGN-094-Sprite_Call", "OGN-105-Singularity", "OGN-141-Kinkou_Monk",
    "OGN-171-Mystic_Poro", "OGN-197-Teemo_Scout", "OGN-210-Daring_Poro", "OGN-220-Facebreaker",
    "OGN-237-Kings_Edict", "OGN-241-Shen_Kinkou", "OGN-242-Baited_Hook", "OGN-260-Last_Breath",
    "OGN-268-Bullet_Time", "OGN-270-Showstopper", "OGN-278-Bandle_Tree", "OGN-285-Reavers_Row",
    "OGS-001-Annie_Fiery", "OGS-005-Zephyr_Sage", "OGS-022-Final_Spark", "SFD-027-Dunebreaker",
    "SFD-045-Not_So_Fast", "SFD-059-Svellsongur", "SFD-086-World_Atlas", "SFD-087-Premonition",
    "SFD-107-Strike_Down", "SFD-122-Called_Shot", "SFD-124-Dorans_Ring", "SFD-157-Royal_Guard",
    "SFD-167-Unsung_Hero", "SFD-182-Danger_Zone", "SFD-214-Power_Nexus", "OGN-005-Disintegrate",
    "OGN-014-Sky_Splitter", "OGN-029-Falling_Star", "OGN-035-Vayne_Hunter",
    "OGN-040-Seal_of_Rage", "OGN-053-Stand_United", "OGN-071-Party_Favors",
    "OGN-079-Leona_Zealot", "OGN-093-Smoke_Screen", "OGN-107-Ava_Achiever",
    "OGN-114-Progress_Day", "OGN-157-Udyr_Wildman", "OGN-164-Sett_Brawler",
    "OGN-183-Stacked_Deck", "OGN-192-Mindsplitter", "OGN-206-Back_to_Back",
    "OGN-208-Cruel_Patron", "OGN-213-Hidden_Blade", "OGN-225-Solari_Chief",
    "OGN-230-Albus_Ferros", "OGN-240-Sett_Kingpin", "OGN-250-Stormbringer",
    "OGN-258-Dragons_Rage", "OGN-262-Zenith_Blade", "OGN-266-Siphon_Power",
    "OGN-289-Targons_Peak", "OGN-298-Zaun_Warrens", "OGS-007-Garen_Rugged",
    "SFD-056-Steraks_Gage", "SFD-095-Dorans_Blade", "SFD-099-Veteran_poro",
    "SFD-111-Here_to_Help", "SFD-112-Kato_the_Arm", "SFD-180-Fiora_Worthy",
    "SFD-186-Spinning_Axe", "OGN-012-Noxus_Hopeful", "OGN-016-Dangerous_Duo",
    "OGN-017-Iron_Ballista", "OGN-052-Stalwart_Poro", "OGN-065-Wizened_Elder",
    "OGN-066-Ahri_Alluring", "OGN-072-Solari_Shrine", "OGN-075-Tasty_Faefolk",
    "OGN-081-Seal_of_Focus", "OGN-085-Falling_Comet", "OGN-090-Orb_of_Regret",
    "OGN-097-Blastcone_Fae", "OGN-100-Gemcraft_Seer", "OGN-102-Portal_Rescue",
    "OGN-106-Sprite_Mother", "OGN-143-Pirates_Haven", "OGN-144-Spoils_of_War",
    "OGN-148-Anivia_Primal", "OGN-150-Kraken_Hunter", "OGN-170-Morbid_Return",
    "OGN-173-Ride_the_Wind", "OGN-198-The_Harrowing", "OGN-207-Call_to_Glory",
    "OGN-209-Cull_the_Weak", "OGN-215-Petty_Officer", "OGN-216-Soaring_Scout",
    "OGN-223-Peak_Guardian", "OGN-228-Vanguard_Helm", "OGN-245-Seal_of_Unity",
    "OGN-246-Viktor_Leader", "OGN-248-Icathian_Rain", "OGN-269-Sett_The_Boss",
    "OGN-281-Hallowed_Tomb", "OGN-295-Vilemaws_Lair", "SFD-024-Rell_Magnetic",
    "SFD-033-Dorans_Shield", "SFD-091-Buhru_Captain", "SFD-108-Warmogs_Armor",
    "SFD-115-Trinity_Force", "SFD-146-Vex_Cheerless", "SFD-155-Honest_Broker",
    "SFD-172-Sacred_Shears", "SFD-179-Corina_Veraza", "SFD-196-Defiant_Dance",
    "OGN-006-Flame_Chompers", "OGN-015-Captain_Farron", "OGN-018-Noxus_Saboteur",
    "OGN-032-Ravenborn_Tome", "OGN-036-Vi_Destructive", "OGN-039-KaiSa_Survivor",
    "OGN-059-Eclipse_Herald", "OGN-063-Spirits_Refuge", "OGN-098-Energy_Conduit",
    "OGN-101-Mushroom_Pouch", "OGN-110-Ekko_Recurrent", "OGN-127-Cannon_Barrage",
    "OGN-142-Mountain_Drake", "OGN-159-Warwick_Hunter", "OGN-186-Treasure_Trove",
    "OGN-189-Kayn_Unleashed", "OGN-190-KogMaw_Caustic", "OGN-222-Noxian_Drummer",
    "OGN-275-Altar_to_Unity", "OGN-277-Back-Alley_Bar", "OGS-010-Annie_Stubborn",
    "OGS-012-Blast_of_Power", "OGS-014-Lux_Crownguard", "SFD-018-Void_Hatchling",
    "SFD-023-Piercing_Light", "SFD-050-Azir_Ascendant", "SFD-051-Guardian_Angel",
    "SFD-057-Irelia_Fervent", "SFD-071-Breakneck_Mech", "SFD-082-Ezreal_Dashing",
    "SFD-140-Fizz_Trickster", "OGN-028-Draven_Showboat", "OGN-049-Playful_Phantom",
    "OGN-054-Sunlit_Guardian", "OGN-073-Sona_Harmonious", "OGN-074-Taric_Protector",
    "OGN-078-Lee_Sin_Ascetic", "OGN-080-Mystic_Reversal", "OGN-096-Watchful_Sentry",
    "OGN-099-Garbage_Grabber", "OGN-109-Dr_Mundo_Expert", "OGN-120-Seal_of_Insight",
    "OGN-123-Unchecked_Power", "OGN-147-Wildclaw_Shaman", "OGN-153-Overt_Operation",
    "OGN-154-Primal_Strength", "OGN-160-Dazzling_Aurora", "OGN-168-Fight_or_Flight",
    "OGN-176-Sneaky_Deckhand", "OGN-180-Fading_Memories", "OGN-181-Pack_of_Wonders",
    "OGN-188-Zaunite_Bouncer", "OGN-204-Seal_of_Discord", "OGN-205-Yasuo_Windrider",
    "OGN-221-Imperial_Decree", "OGN-226-Spectral_Matron", "OGN-233-Grand_Strategem",
    "OGN-235-Karma_Channeler", "OGN-236-Karthus_Eternal", "OGN-239-Machine_Evangel",
    "OGN-244-Divine_Judgment", "OGN-276-Aspirants_Climb", "OGN-286-Reckoners_Arena",
    "OGN-288-Startipped_Peak", "OGN-293-The_Grand_Plaza", "OGS-006-Lux_Illuminated",
    "OGS-008-Gentlemens_Duel", "OGS-009-Master_Yi_Honed", "OGS-013-Garen_Commander",
    "OGS-024-Decisive_Strike", "SFD-025-Rengar_Pouncing", "SFD-039-Royal_Entourage",
    "SFD-114-Marching_Orders", "SFD-119-Jax_Unrelenting", "SFD-120-Sivir_Ambitious",
    "SFD-128-Overzealous_Fan", "SFD-130-Treasure_Hunter", "SFD-141-Irelia_Graceful",
    "SFD-168-Vanguard_Armory", "SFD-173-Soraka_Wanderer", "SFD-183-Lucian_Purifier",
    "OGN-001-Blazing_Scorcher", "OGN-002-Brazen_Buccaneer", "OGN-010-Legion_Rearguard",
    "OGN-027-Darius_Trifarian", "OGN-031-Raging_Firebrand", "OGN-037-Immortal_Phoenix",
    "OGN-041-Volibear_Furious", "OGN-044-Clockwork_Keeper", "OGN-047-Find_Your_Center",
    "OGN-055-Wielder_of_Water", "OGN-076-Yasuo_Remorseful", "OGN-083-Consult_the_Past",
    "OGN-084-Eager_Apprentice", "OGN-086-Jeweled_Colossus", "OGN-087-Lecturing_Yordle",
    "OGN-113-Malzahar_Fanatic", "OGN-115-Promising_Future", "OGN-117-Viktor_Innovator",
    "OGN-118-Wraith_of_Echoes", "OGN-119-Ahri_Inquisitive", "OGN-121-Teemo_Strategist",
    "OGN-125-Bilgewater_Bully", "OGN-133-Flurry_of_Blades", "OGN-137-Stormclaw_Ursine",
    "OGN-140-Herald_of_Scales", "OGN-151-Lee_Sin_Centered", "OGN-163-Seal_of_Strength",
    "OGN-175-Shipyard_Skulker", "OGN-177-Stealthy_Pursuer", "OGN-178-Undercover_Agent",
    "OGN-201-Invert_Timelines", "OGN-218-Vanguard_Captain", "OGN-231-Commander_Ledros",
    "OGN-232-Fiora_Victorious", "OGN-234-Harnessed_Dragon", "OGN-238-Leona_Determined",
    "OGN-259-Yasuo_Unforgiven", "OGN-264-Guerilla_Warfare", "OGN-284-Obelisk_of_Power",
    "OGS-017-Annie_Dark_Child", "SFD-001-Against_the_Odds", "SFD-049-Aphelios_Exalted",
    "SFD-113-Lucian_Merciless", "SFD-116-Yone_Blademaster", "SFD-148-Draven_Audacious",
    "OGN-003-Chemtech_Enforcer", "OGN-023-Unlicensed_Armory", "OGN-060-Mask_of_Foresight",
    "OGN-070-Mageseeker_Warden", "OGN-077-Zhonyas_Hourglass", "OGN-130-Crackshot_Corsair",
    "OGN-138-Catalyst_of_Aeons", "OGN-145-Unyielding_Spirit", "OGN-155-Qiyana_Victorious",
    "OGN-158-Volibear_Imposing", "OGN-179-Acceptable_Losses", "OGN-191-Maddened_Marauder",
    "OGN-219-Vanguard_Sergeant", "OGN-251-Jinx_Loose_Cannon", "OGN-254-Noxian_Guillotine",
    "OGN-263-Teemo_Swift_Scout", "OGN-292-The_Dreaming_Tree", "OGN-297-Windswept_Hillock",
    "SFD-020-Draven_Vanquisher", "SFD-048-Stellacorn_herder", "SFD-075-Prize_of_Progress",
    "SFD-151-Bonds_of_Strength", "SFD-169-Altar_of_Memories", "OGN-020-Scrapyard_Champion",
    "OGN-030-Jinx_Demolitionist", "OGN-068-Caitlyn_Patrolling", "OGN-103-Ravenbloom_Student",
    "OGN-112-KaiSa_Evolutionary", "OGN-161-Deadbloom_Predator", "OGN-165-Cemetery_Attendant",
    "OGN-185-Traveling_Merchant", "OGN-195-Rhasa_the_Sunderer", "OGN-243-Darius_Executioner",
    "OGN-257-Lee_Sin_Blind_Monk", "OGN-261-Leona_Radiant_Dawn", "OGN-279-Fortified_Position",
    "OGN-287-Sigil_of_the_Storm", "OGN-294-Trifarian_War_Camp", "OGS-016-Vanguard_Attendant",
    "SFD-093-Dauntless_Vanguard", "SFD-133-Boots_of_Swiftness", "SFD-170-RekSai_Swarm_Queen",
    "SFD-184-Relentless_Pursuit", "OGN-026-Brynhir_Thundersong", "OGN-051-Solari_Shieldbearer",
    "OGN-108-Convergent_Mutation", "OGN-194-Nocturne_Horrifying", "OGN-211-Faithful_Manufactor",
    "OGN-212-Forge_of_the_Future", "OGN-282-Monastery_of_Hirana", "OGN-283-Navori_Fighting_Pit",
    "OGN-290-The_Arenas_Greatest", "SFD-195-Irelia_Blade_Dancer", "SFD-205-Fiora_Grand_Duelist",
    "OGN-034-Tryndamere_Barbarian", "OGN-067-Blitzcrank_Impassive", "OGN-082-Whiteflame_Protector",
    "OGN-149-Carnivorous_Snapvine", "OGN-162-Miss_Fortune_Captain", "OGN-200-Twisted_Fate_Gambler",
    "OGN-227-Symbol_of_the_Solari", "OGN-253-Darius_Hand_of_Noxus", "OGN-255-Ahri_Nine-Tailed_Fox",
    "OGS-004-Master_Yi_Meditative", "OGS-015-Recruit_the_Vanguard", "SFD-187-RekSai_Void_Burrower",
    "OGN-111-Heimerdinger_Inventor", "OGN-139-Cithria_of_Cloudfield", "OGN-217-Trifarian_Gloryseeker",
    "OGN-291-The_Candlelit_Sanctum", "SFD-203-Sivir_Battle_Mistress", "OGN-038-Kadregrin_the_Infernal",
    "OGN-193-Miss_Fortune_Buccaneer", "OGS-021-Lux_Lady_of_Luminosity", "OGS-023-Garen_Might_of_Demacia",
    "OGN-116-Thousand-Tailed_Watcher", "OGN-252-Super_Mega_Death_Rocket", "OGN-280-Grove_of_the_God-Willow",
    "SFD-055-Needlessly_Large_Yordle", "SFD-193-Jax_Grandmaster_at_Arms", "SFD-215-Ravenbloom_Conservatory",
    "OGS-019-Master_Yi_Wuju_Bladesman", "SFD-178-Blade_of_the_Ruined_King", "SFD-181-Rumble_Mechanized_Menace",
    "SFD-199-Ezreal_Prodigal_Explorer", "OGN-249-Volibear_Relentless_Storm", "SFD-197-Azir_Emperor_of_the_Sands",
    "OGN-247-KaiSa_Daughter_of_the_Void", "OGN-267-Miss_Fortune_Bounty_Hunter", "SFD-171-Renata_Glasc_Industrialist",
    "SFD-201-Renata_Glasc_Chem-Baroness", "OGN-265-Viktor_Herald_of_the_Arcane", "SFD-185-Draven_Glorious_Executioner",
    "SFD-189-Ornn_Fire_Below_the_Mountain"
]

cards_file_path = 'app/src/main/assets/cards.json'

try:
    with open(cards_file_path, 'r') as f:
        existing_cards = json.load(f)
except FileNotFoundError:
    existing_cards = []

existing_card_keys = set()
for card in existing_cards:
    # Key to uniquely identify a card (Set + Number)
    key = f"{card['set']}-{card['card_number']}"
    existing_card_keys.add(key)

new_cards = []
for folder in card_folders:
    parts = folder.split('-')
    if len(parts) >= 3:
        card_set = parts[0]
        card_number = parts[1]
        card_name = ' '.join(parts[2:]).replace('_', ' ')
        
        key = f"{card_set}-{card_number}"
        
        if key not in existing_card_keys:
            new_cards.append({'set': card_set, 'card_number': card_number, 'name': card_name})

# Extend the existing list with new cards
all_cards = existing_cards + new_cards

# Sort the cards by Set and then by Card Number
sorted_cards = sorted(all_cards, key=lambda k: (k['set'], k['card_number']))

# Write back to the file
with open(cards_file_path, 'w') as f:
    json.dump(sorted_cards, f, indent=4)

print(f"Added {len(new_cards)} new cards.")
print("Successfully updated and sorted cards.json")

package com.gyosanila.logichild.assets

import androidx.annotation.DrawableRes
import com.gyosanila.logichild.R

/**
 * Katalog objek untuk mini game worksheet (Kelompokkan, Hubungkan Pasangan,
 * Bandingkan Ukuran, Cari yang Berbeda).
 *
 * Semua ikon digambar dari Twemoji 15.1.0 (CC-BY 4.0), dikonversi ke Android
 * vector drawable, sehingga tidak bergantung pada font emoji device.
 *
 * Atribusi wajib dicantumkan di layar About/Tentang:
 * "Ikon: Twemoji (CC-BY 4.0) — github.com/jdecked/twemoji"
 */
enum class ObjCategory { HEWAN, BUAH, SAYUR, KENDARAAN, PAKAIAN, MAKANAN, ALAT_MUSIK, FURNITUR, ALAT_TULIS, ALAM, TUBUH, PERALATAN }

data class GameObject(
    val id: String,
    val labelId: String,
    val labelEn: String,
    val category: ObjCategory,
    @DrawableRes val icon: Int,
    /** Ukuran dunia nyata relatif, 1..3. Dipakai game Bandingkan Ukuran. */
    val sizeRank: Int = 2,
)

/** Semua objek yang tersedia. Dipakai sebagai bank soal lintas mini game. */
val OBJECTS: List<GameObject> = listOf(
    // ── HEWAN ─────────────────────────────────────────────
    GameObject("cat", "Kucing", "Cat", ObjCategory.HEWAN, R.drawable.ic_cat, 2),
    GameObject("dog", "Anjing", "Dog", ObjCategory.HEWAN, R.drawable.ic_dog, 2),
    GameObject("mouse", "Tikus", "Mouse", ObjCategory.HEWAN, R.drawable.ic_mouse, 1),
    GameObject("elephant", "Gajah", "Elephant", ObjCategory.HEWAN, R.drawable.ic_elephant, 3),
    GameObject("rabbit", "Kelinci", "Rabbit", ObjCategory.HEWAN, R.drawable.ic_rabbit, 2),
    GameObject("bear", "Beruang", "Bear", ObjCategory.HEWAN, R.drawable.ic_bear, 3),
    GameObject("lion", "Singa", "Lion", ObjCategory.HEWAN, R.drawable.ic_lion, 3),
    GameObject("giraffe", "Jerapah", "Giraffe", ObjCategory.HEWAN, R.drawable.ic_giraffe, 3),
    GameObject("monkey", "Monyet", "Monkey", ObjCategory.HEWAN, R.drawable.ic_monkey, 2),
    GameObject("turtle", "Kura-kura", "Turtle", ObjCategory.HEWAN, R.drawable.ic_turtle, 1),
    GameObject("bird", "Burung", "Bird", ObjCategory.HEWAN, R.drawable.ic_bird, 1),
    GameObject("fish", "Ikan", "Fish", ObjCategory.HEWAN, R.drawable.ic_fish, 1),
    GameObject("whale", "Paus", "Whale", ObjCategory.HEWAN, R.drawable.ic_whale, 3),
    GameObject("bee", "Lebah", "Bee", ObjCategory.HEWAN, R.drawable.ic_bee, 1),
    GameObject("butterfly", "Kupu-kupu", "Butterfly", ObjCategory.HEWAN, R.drawable.ic_butterfly, 1),
    GameObject("ant", "Semut", "Ant", ObjCategory.HEWAN, R.drawable.ic_ant, 1),
    GameObject("caterpillar", "Ulat", "Caterpillar", ObjCategory.HEWAN, R.drawable.ic_caterpillar, 1),
    GameObject("panda", "Panda", "Panda", ObjCategory.HEWAN, R.drawable.ic_panda, 3),
    GameObject("fox", "Rubah", "Fox", ObjCategory.HEWAN, R.drawable.ic_fox, 2),
    GameObject("koala", "Koala", "Koala", ObjCategory.HEWAN, R.drawable.ic_koala, 2),
    GameObject("frog", "Katak", "Frog", ObjCategory.HEWAN, R.drawable.ic_frog, 1),
    GameObject("penguin", "Penguin", "Penguin", ObjCategory.HEWAN, R.drawable.ic_penguin, 2),
    GameObject("dolphin", "Lumba-lumba", "Dolphin", ObjCategory.HEWAN, R.drawable.ic_dolphin, 3),
    GameObject("duck", "Bebek", "Duck", ObjCategory.HEWAN, R.drawable.ic_duck, 2),
    GameObject("chicken", "Ayam", "Chicken", ObjCategory.HEWAN, R.drawable.ic_chicken, 2),

    // ── BUAH ──────────────────────────────────────────────
    GameObject("apple", "Apel", "Apple", ObjCategory.BUAH, R.drawable.ic_apple, 2),
    GameObject("banana", "Pisang", "Banana", ObjCategory.BUAH, R.drawable.ic_banana, 2),
    GameObject("orange", "Jeruk", "Orange", ObjCategory.BUAH, R.drawable.ic_orange, 2),
    GameObject("watermelon", "Semangka", "Watermelon", ObjCategory.BUAH, R.drawable.ic_watermelon, 3),
    GameObject("strawberry", "Stroberi", "Strawberry", ObjCategory.BUAH, R.drawable.ic_strawberry, 1),
    GameObject("grapes", "Anggur", "Grapes", ObjCategory.BUAH, R.drawable.ic_grapes, 2),
    GameObject("mango", "Mangga", "Mango", ObjCategory.BUAH, R.drawable.ic_mango, 2),
    GameObject("pineapple", "Nanas", "Pineapple", ObjCategory.BUAH, R.drawable.ic_pineapple, 3),
    GameObject("cherry", "Ceri", "Cherry", ObjCategory.BUAH, R.drawable.ic_cherry, 1),
    GameObject("lemon", "Lemon", "Lemon", ObjCategory.BUAH, R.drawable.ic_lemon, 1),
    GameObject("peach", "Persik", "Peach", ObjCategory.BUAH, R.drawable.ic_peach, 2),
    GameObject("pear", "Pir", "Pear", ObjCategory.BUAH, R.drawable.ic_pear, 2),
    GameObject("kiwi", "Kiwi", "Kiwi", ObjCategory.BUAH, R.drawable.ic_kiwi, 1),

    // ── SAYUR ─────────────────────────────────────────────
    GameObject("carrot", "Wortel", "Carrot", ObjCategory.SAYUR, R.drawable.ic_carrot, 2),
    GameObject("broccoli", "Brokoli", "Broccoli", ObjCategory.SAYUR, R.drawable.ic_broccoli, 2),
    GameObject("tomato", "Tomat", "Tomato", ObjCategory.SAYUR, R.drawable.ic_tomato, 1),
    GameObject("corn", "Jagung", "Corn", ObjCategory.SAYUR, R.drawable.ic_corn, 2),
    GameObject("cucumber", "Timun", "Cucumber", ObjCategory.SAYUR, R.drawable.ic_cucumber, 2),
    GameObject("eggplant", "Terong", "Eggplant", ObjCategory.SAYUR, R.drawable.ic_eggplant, 2),
    GameObject("pepper", "Paprika", "Pepper", ObjCategory.SAYUR, R.drawable.ic_pepper, 1),
    GameObject("potato", "Kentang", "Potato", ObjCategory.SAYUR, R.drawable.ic_potato, 1),
    GameObject("mushroom", "Jamur", "Mushroom", ObjCategory.SAYUR, R.drawable.ic_mushroom, 1),

    // ── KENDARAAN ─────────────────────────────────────────
    GameObject("car", "Mobil", "Car", ObjCategory.KENDARAAN, R.drawable.ic_car, 2),
    GameObject("bus", "Bus", "Bus", ObjCategory.KENDARAAN, R.drawable.ic_bus, 3),
    GameObject("bicycle", "Sepeda", "Bicycle", ObjCategory.KENDARAAN, R.drawable.ic_bicycle, 1),
    GameObject("airplane", "Pesawat", "Airplane", ObjCategory.KENDARAAN, R.drawable.ic_airplane, 3),
    GameObject("train", "Kereta", "Train", ObjCategory.KENDARAAN, R.drawable.ic_train, 3),
    GameObject("ship", "Kapal", "Ship", ObjCategory.KENDARAAN, R.drawable.ic_ship, 3),
    GameObject("motorcycle", "Motor", "Motorcycle", ObjCategory.KENDARAAN, R.drawable.ic_motorcycle, 2),
    GameObject("helicopter", "Helikopter", "Helicopter", ObjCategory.KENDARAAN, R.drawable.ic_helicopter, 3),
    GameObject("scooter", "Skuter", "Scooter", ObjCategory.KENDARAAN, R.drawable.ic_scooter, 1),
    GameObject("boat", "Perahu", "Boat", ObjCategory.KENDARAAN, R.drawable.ic_boat, 2),
    GameObject("rocket", "Roket", "Rocket", ObjCategory.KENDARAAN, R.drawable.ic_rocket, 3),

    // ── PAKAIAN ───────────────────────────────────────────
    GameObject("shirt", "Baju", "Shirt", ObjCategory.PAKAIAN, R.drawable.ic_shirt, 2),
    GameObject("shorts", "Celana", "Shorts", ObjCategory.PAKAIAN, R.drawable.ic_shorts, 2),
    GameObject("cap", "Topi", "Cap", ObjCategory.PAKAIAN, R.drawable.ic_cap, 1),
    GameObject("shoe", "Sepatu", "Shoe", ObjCategory.PAKAIAN, R.drawable.ic_shoe, 2),
    GameObject("sock", "Kaus kaki", "Sock", ObjCategory.PAKAIAN, R.drawable.ic_sock, 1),
    GameObject("glove", "Sarung tangan", "Glove", ObjCategory.PAKAIAN, R.drawable.ic_glove, 1),
    GameObject("glasses", "Kacamata", "Glasses", ObjCategory.PAKAIAN, R.drawable.ic_glasses, 1),

    // ── MAKANAN ───────────────────────────────────────────
    GameObject("icecream", "Es krim", "Ice cream", ObjCategory.MAKANAN, R.drawable.ic_icecream, 2),
    GameObject("pizza", "Pizza", "Pizza", ObjCategory.MAKANAN, R.drawable.ic_pizza, 2),
    GameObject("honey", "Madu", "Honey", ObjCategory.MAKANAN, R.drawable.ic_honey, 1),
    GameObject("milk", "Susu", "Milk", ObjCategory.MAKANAN, R.drawable.ic_milk, 2),
    GameObject("babybottle", "Botol susu", "Baby bottle", ObjCategory.MAKANAN, R.drawable.ic_babybottle, 1),
    GameObject("cheese", "Keju", "Cheese", ObjCategory.MAKANAN, R.drawable.ic_cheese, 1),
    GameObject("bread", "Roti", "Bread", ObjCategory.MAKANAN, R.drawable.ic_bread, 2),
    GameObject("egg", "Telur", "Egg", ObjCategory.MAKANAN, R.drawable.ic_egg, 1),
    GameObject("cake", "Kue", "Cake", ObjCategory.MAKANAN, R.drawable.ic_cake, 2),
    GameObject("cookie", "Biskuit", "Cookie", ObjCategory.MAKANAN, R.drawable.ic_cookie, 1),

    // ── ALAT MUSIK ────────────────────────────────────────
    GameObject("guitar", "Gitar", "Guitar", ObjCategory.ALAT_MUSIK, R.drawable.ic_guitar, 2),
    GameObject("piano", "Piano", "Piano", ObjCategory.ALAT_MUSIK, R.drawable.ic_piano, 3),
    GameObject("drum", "Drum", "Drum", ObjCategory.ALAT_MUSIK, R.drawable.ic_drum, 2),
    GameObject("trumpet", "Terompet", "Trumpet", ObjCategory.ALAT_MUSIK, R.drawable.ic_trumpet, 1),
    GameObject("violin", "Biola", "Violin", ObjCategory.ALAT_MUSIK, R.drawable.ic_violin, 1),
    GameObject("bell", "Bel", "Bell", ObjCategory.ALAT_MUSIK, R.drawable.ic_bell, 1),

    // ── FURNITUR ──────────────────────────────────────────
    GameObject("chair", "Kursi", "Chair", ObjCategory.FURNITUR, R.drawable.ic_chair, 2),
    GameObject("bed", "Kasur", "Bed", ObjCategory.FURNITUR, R.drawable.ic_bed, 3),
    GameObject("door", "Pintu", "Door", ObjCategory.FURNITUR, R.drawable.ic_door, 3),
    GameObject("window", "Jendela", "Window", ObjCategory.FURNITUR, R.drawable.ic_window, 2),
    GameObject("lamp", "Lampu", "Lamp", ObjCategory.FURNITUR, R.drawable.ic_lamp, 1),
    GameObject("clock", "Jam", "Clock", ObjCategory.FURNITUR, R.drawable.ic_clock, 1),
    GameObject("tv", "Televisi", "Television", ObjCategory.FURNITUR, R.drawable.ic_tv, 2),

    // ── ALAT TULIS ────────────────────────────────────────
    GameObject("pencil", "Pensil", "Pencil", ObjCategory.ALAT_TULIS, R.drawable.ic_pencil, 1),
    GameObject("paper", "Kertas", "Paper", ObjCategory.ALAT_TULIS, R.drawable.ic_paper, 2),
    GameObject("crayon", "Krayon", "Crayon", ObjCategory.ALAT_TULIS, R.drawable.ic_crayon, 1),
    GameObject("book", "Buku", "Book", ObjCategory.ALAT_TULIS, R.drawable.ic_book, 2),
    GameObject("scissors", "Gunting", "Scissors", ObjCategory.ALAT_TULIS, R.drawable.ic_scissors, 1),

    // ── ALAM ──────────────────────────────────────────────
    GameObject("umbrella", "Payung", "Umbrella", ObjCategory.ALAM, R.drawable.ic_umbrella, 2),
    GameObject("rain", "Hujan", "Rain", ObjCategory.ALAM, R.drawable.ic_rain, 2),
    GameObject("sun", "Matahari", "Sun", ObjCategory.ALAM, R.drawable.ic_sun, 3),
    GameObject("moon", "Bulan", "Moon", ObjCategory.ALAM, R.drawable.ic_moon, 3),
    GameObject("star", "Bintang", "Star", ObjCategory.ALAM, R.drawable.ic_star, 1),
    GameObject("rainbow", "Pelangi", "Rainbow", ObjCategory.ALAM, R.drawable.ic_rainbow, 3),
    GameObject("cloud", "Awan", "Cloud", ObjCategory.ALAM, R.drawable.ic_cloud, 3),
    GameObject("fire", "Api", "Fire", ObjCategory.ALAM, R.drawable.ic_fire, 2),
    GameObject("ice", "Es", "Ice", ObjCategory.ALAM, R.drawable.ic_ice, 1),
    GameObject("drop", "Air", "Water", ObjCategory.ALAM, R.drawable.ic_drop, 1),
    GameObject("tree", "Pohon", "Tree", ObjCategory.ALAM, R.drawable.ic_tree, 3),
    GameObject("nest", "Sarang", "Nest", ObjCategory.ALAM, R.drawable.ic_nest, 1),
    GameObject("flower", "Bunga", "Flower", ObjCategory.ALAM, R.drawable.ic_flower, 1),
    GameObject("leaf", "Daun", "Leaf", ObjCategory.ALAM, R.drawable.ic_leaf, 1),
    GameObject("snowflake", "Salju", "Snowflake", ObjCategory.ALAM, R.drawable.ic_snowflake, 1),
    GameObject("lightning", "Petir", "Lightning", ObjCategory.ALAM, R.drawable.ic_lightning, 2),

    // ── TUBUH ─────────────────────────────────────────────
    GameObject("foot", "Kaki", "Foot", ObjCategory.TUBUH, R.drawable.ic_foot, 1),
    GameObject("hand", "Tangan", "Hand", ObjCategory.TUBUH, R.drawable.ic_hand, 1),
    GameObject("tooth", "Gigi", "Tooth", ObjCategory.TUBUH, R.drawable.ic_tooth, 1),
    GameObject("eye", "Mata", "Eye", ObjCategory.TUBUH, R.drawable.ic_eye, 1),
    GameObject("ear", "Telinga", "Ear", ObjCategory.TUBUH, R.drawable.ic_ear, 1),

    // ── PERALATAN ─────────────────────────────────────────
    GameObject("toothbrush", "Sikat gigi", "Toothbrush", ObjCategory.PERALATAN, R.drawable.ic_toothbrush, 1),
    GameObject("toothpaste", "Pasta gigi", "Toothpaste", ObjCategory.PERALATAN, R.drawable.ic_toothpaste, 1),
    GameObject("key", "Kunci", "Key", ObjCategory.PERALATAN, R.drawable.ic_key, 1),
    GameObject("spoon", "Sendok", "Spoon", ObjCategory.PERALATAN, R.drawable.ic_spoon, 1),
    GameObject("fork", "Garpu", "Fork", ObjCategory.PERALATAN, R.drawable.ic_fork, 1),
    GameObject("cup", "Gelas", "Glass", ObjCategory.PERALATAN, R.drawable.ic_cup, 1),
    GameObject("ball", "Bola", "Ball", ObjCategory.PERALATAN, R.drawable.ic_ball, 2),
    GameObject("puzzle", "Puzzle", "Puzzle", ObjCategory.PERALATAN, R.drawable.ic_puzzle, 1),
    GameObject("teddy", "Boneka", "Teddy", ObjCategory.PERALATAN, R.drawable.ic_teddy, 2),
    GameObject("hammer", "Palu", "Hammer", ObjCategory.PERALATAN, R.drawable.ic_hammer, 1),
    GameObject("broom", "Sapu", "Broom", ObjCategory.PERALATAN, R.drawable.ic_broom, 2),
    GameObject("soap", "Sabun", "Soap", ObjCategory.PERALATAN, R.drawable.ic_soap, 1),
    GameObject("bucket", "Ember", "Bucket", ObjCategory.PERALATAN, R.drawable.ic_bucket, 2),
    GameObject("basket", "Keranjang", "Basket", ObjCategory.PERALATAN, R.drawable.ic_basket, 2),
)

/** Ambil objek per kategori. */
fun objectsOf(category: ObjCategory): List<GameObject> = OBJECTS.filter { it.category == category }

/** Ambil objek berdasarkan id. */
fun objectById(id: String): GameObject? = OBJECTS.firstOrNull { it.id == id }

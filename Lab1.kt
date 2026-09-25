fun isPrime(num: Int): Boolean {
    if (num < 2) return false
    for (i in 2..num - 1) {
        if (num % i == 0) return false
    }
    return true
}
fun encode(msg: String): String {
    return msg.reversed()
}
fun decode(msg: String): String {
    return msg.reversed() 
}
fun messageCoding(msg: String, func: (String) -> String): String {
    return func(msg) 
}
fun printEvenNumbers(lista: List<Int>) = println(lista.filter { szam -> szam % 2 == 0 })

fun groupAnagrams(szavak: Array<String>): List<List<String>> {
    // A groupBy szétválogatja a szavakat. A szabály: 
    // 1. kisbetű (lowercase)
    // 2. szétszedés karakterekre (toList)
    // 3. ABC sorrend (sorted)
    return szavak.groupBy { szo -> szo.lowercase().toList().sorted() }.values.toList()
}

fun main() {
    println("\n1.FELADAT")
    val a = 2
    val b = 3
    println("$a + $b = ${a + b}")

    println("\n2.FELADAT")
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    for (day in daysOfWeek) {
        println(day)
    }

    println("T-vel kezdodo: " + daysOfWeek.filter { nap -> nap.startsWith("T") })
    println("van benne e betu : " + daysOfWeek.filter { nap -> nap.contains("e") })
    println("6 betubol all : " + daysOfWeek.filter { nap -> nap.length == 6 })

    println("\n3.FELADAT")
    for (i in 1..20) {
        if (isPrime(i)) {
            print("$i ")
        }
    }

    println("\n4.FELADAT")
    //A :: (két kettőspont) a Kotlinban arra való, hogy rámutass egy parancsra vagy gépre, 
    //ahelyett, hogy azonnal elindítanád.
    val eredetiSzoveg = "TITKOS UGYNOK"
    println("Eredeti: " + eredetiSzoveg)

    val titkositott = messageCoding(eredetiSzoveg, ::encode)
    println("Titkositva: " + titkositott)
    
    val visszafejtett = messageCoding(titkositott, ::decode)
    println("Visszafejtve: " + visszafejtett)

    println("\n5.FELADAT")
    val szamok = listOf(1, 2, 3, 4, 5, 6, 7, 8) 
    print("Paros szamok: ")
    printEvenNumbers(szamok) 

    println("\n6.FELADAT")

    println("Duplazott szamok: " + szamok.map { szam -> szam * 2 })
    
    println("Nagybetus napok: " + daysOfWeek.map { nap -> nap.uppercase() })
    
    println("Kezdobetuk: " + daysOfWeek.map { nap -> nap.first().uppercase() })
    
    val hosszak = daysOfWeek.map { nap -> nap.length }
    println("Napok hossza: " + hosszak)
    
    println("Atlagos hossz: " + hosszak.average())

    println("\n7.FELADAT")
    val days = daysOfWeek.toMutableList()
    days.removeIf{nap-> nap.contains("n")}
    println(days)

    for((index,nap) in days.withIndex()){
        println("Item at $index is $nap")
    }
    
    days.sort()
    println(days)

    println("\n8.FELADAT")
    val randomSzamok = Array(10) { (0..100).random() }

    println("Eredeti szamok:")
    randomSzamok.forEach { szam -> print("$szam ") }
    println()
    
    randomSzamok.sort()
    println("Sorba rendezve:")
    randomSzamok.forEach { szam -> print("$szam ") }
    println()
    
    val vanParos = randomSzamok.any { szam -> szam % 2 == 0 }
    println("Van benne paros szam? " + vanParos)
    
    val mindParos = randomSzamok.all { szam -> szam % 2 == 0 }
    println("Mindegyik szam paros? " + mindParos)
    
    println("A szamok atlaga: " + randomSzamok.average())

    println("\nEXTRA FELADAT: Anagrammak")
    
    // Kipróbáljuk a képen látható nagybetűs-kisbetűs kevert listával
    val tesztSzavak = arrayOf("eat", "tEa", "Tan", "atE", "NAT", "bat")
    val csoportok = groupAnagrams(tesztSzavak)
    
    println("Eredmeny: ")
    for (csoport in csoportok) {
        println(csoport)
    }
}
package sn.mo.ussdapp.data

data class UssdService(
    val name: String,
    val ussdCode: String
)

data class Operator(
    val key: String,
    val label: String,
    val accentColor: Long,
    val services: List<UssdService>
)

object Operators {

    val ORANGE = Operator(
        key = "orange",
        label = "Orange",
        accentColor = 0xFFFF6600,
        services = listOf(
            UssdService("Voir solde credit", "#123#"),
            UssdService("Achat de credits/pass internet", "#1234#"),
            UssdService("Transfert de credit", "#999#")
        )
    )

    val YAS = Operator(
        key = "yas",
        label = "Yas",
        accentColor = 0xFF5B2A86,
        services = listOf(
            UssdService("Consulter le solde", "#176#"),
            UssdService("Achat de forfait", "#155#")
        )
    )

    val EXPRESSO = Operator(
        key = "expresso",
        label = "Expresso",
        accentColor = 0xFF00A651,
        services = listOf(
            UssdService("Pass Expresso", "*5#"),
            UssdService("SOS Expresso", "#212#"),
            UssdService("Solde Expresso", "*222#")
        )
    )

    val ORANGE_MONEY = Operator(
        key = "orangemoney",
        label = "Orange Money",
        accentColor = 0xFFF7941D,
        services = listOf(
            UssdService("Solde de mes comptes", "#144*1#"),
            UssdService("Transfert", "#144*2#"),
            UssdService("Paiement de facture", "#144*3#"),
            UssdService("Achats : Credit et Pass", "#144*4#"),
            UssdService("Paiement biens et services", "#144*5#"),
            UssdService("Dons et Cotisations", "#144*6#"),
            UssdService("Option", "#144*7#"),
            UssdService("Banques", "#144*8#"),
            UssdService("Jeux", "#144*9#"),
            UssdService("Orange Bank", "#144*10#"),
            UssdService("Validation de trans.", "#144#")
        )
    )

    val WAVE = Operator(
        key = "wave",
        label = "Wave",
        accentColor = 0xFF1DC8F2,
        services = listOf(
            UssdService("Envoyer de l'argent", "#141*1#"),
            UssdService("Achat credit", "#141*2#"),
            UssdService("Achat Woyofal", "#141*3#"),
            UssdService("Verifiez le solde et transactions", "#141*0#")
        )
    )

    val ALL = listOf(ORANGE, YAS, EXPRESSO, ORANGE_MONEY, WAVE)
}

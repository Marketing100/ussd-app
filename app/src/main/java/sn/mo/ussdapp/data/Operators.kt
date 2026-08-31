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
            UssdService("Transfert de credit", "#999#"),
            UssdService("Connaitre son numero", "#237#"),
            UssdService("Transfert credit et bonus", "#116#"),
            UssdService("Gestion des options et services", "#145#"),
            UssdService("Orange Sargal (fidelite)", "#221#")
        )
    )

    val YAS = Operator(
        key = "yas",
        label = "Yas",
        accentColor = 0xFF5B2A86,
        services = listOf(
            UssdService("Consulter le solde", "#176#"),
            UssdService("Achat de forfait", "#155#"),
            UssdService("Lebalma (credit d'urgence)", "#188#"),
            UssdService("Compte Mixx", "#150#")
        )
    )

    val EXPRESSO = Operator(
        key = "expresso",
        label = "Expresso",
        accentColor = 0xFF00A651,
        services = listOf(
            UssdService("Pass Expresso", "*5#"),
            UssdService("SOS Expresso", "#212#"),
            UssdService("Solde Expresso", "*222#"),
            UssdService("Portail Expresso", "*1111#")
        )
    )

    val ORANGE_MONEY = Operator(
        key = "orangemoney",
        label = "Orange Money",
        accentColor = 0xFFF7941D,
        services = listOf(
            UssdService("Solde de mes comptes", "#144#1#"),
            UssdService("Transfert", "#144#2#"),
            UssdService("Paiement de facture", "#144#3#"),
            UssdService("Achats : Credit et Pass", "#144#4#"),
            UssdService("Paiement biens et services", "#144#5#"),
            UssdService("Dons et Cotisations", "#144#6#"),
            UssdService("Option", "#144#7#"),
            UssdService("Banques", "#144#8#"),
            UssdService("Jeux", "#144#9#"),
            UssdService("Orange Bank", "#144#10#"),
            UssdService("Validation de trans.", "#144#"),
            UssdService("Rechargement Woyofal", "#144#342#"),
            UssdService("Ouverture de compte", "#144#75#")
        )
    )

    val WAVE = Operator(
        key = "wave",
        label = "Wave",
        accentColor = 0xFF1DC8F2,
        services = listOf(
            UssdService("Envoyer de l'argent", "#2171#1#"),
            UssdService("Achat credit", "#2171#2#"),
            UssdService("Achat Woyofal", "#2171#3#"),
            UssdService("Verifiez le solde et transactions", "#2171#0#")
        )
    )

    val PROMOBILE = Operator(
        key = "promobile",
        label = "Promobile",
        accentColor = 0xFF7A1FA2,
        services = listOf(
            UssdService("Solde de credit", "#123#"),
            UssdService("Suivi consommation", "#1175#")
        )
    )

    val ALL = listOf(ORANGE, YAS, EXPRESSO, ORANGE_MONEY, WAVE, PROMOBILE)
}

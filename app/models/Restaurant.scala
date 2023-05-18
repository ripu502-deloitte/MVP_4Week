package models

case class Restaurant(
                       restaurantName: String,
                       cuisine: String,
                       openHours: String,
                       state: String,
                       cntyGeoid: String,
                       cntyName: String,
                       uaGeoid: String,
                       uaName: String,
                       msaGeoid: String,
                       msaName: String,
                       lon: String,
                       lat: String,
                       frequency: String,
                       isChain: String
                     )
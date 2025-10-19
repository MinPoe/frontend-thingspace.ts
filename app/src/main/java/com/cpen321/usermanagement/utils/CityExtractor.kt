package com.cpen321.usermanagement.utils

object CityExtractor {
    
    private val cityKeywords = mapOf(
        // Canadian cities
        "vancouver" to "Vancouver, BC, Canada",
        "toronto" to "Toronto, ON, Canada", 
        "montreal" to "Montreal, QC, Canada",
        "calgary" to "Calgary, AB, Canada",
        "ottawa" to "Ottawa, ON, Canada",
        "edmonton" to "Edmonton, AB, Canada",
        "winnipeg" to "Winnipeg, MB, Canada",
        "quebec" to "Quebec City, QC, Canada",
        "hamilton" to "Hamilton, ON, Canada",
        "london" to "London, ON, Canada",
        
        // US cities
        "new york" to "New York, NY, USA",
        "los angeles" to "Los Angeles, CA, USA",
        "chicago" to "Chicago, IL, USA",
        "houston" to "Houston, TX, USA",
        "phoenix" to "Phoenix, AZ, USA",
        "philadelphia" to "Philadelphia, PA, USA",
        "san antonio" to "San Antonio, TX, USA",
        "san diego" to "San Diego, CA, USA",
        "dallas" to "Dallas, TX, USA",
        "san jose" to "San Jose, CA, USA",
        "austin" to "Austin, TX, USA",
        "jacksonville" to "Jacksonville, FL, USA",
        "fort worth" to "Fort Worth, TX, USA",
        "columbus" to "Columbus, OH, USA",
        "charlotte" to "Charlotte, NC, USA",
        "seattle" to "Seattle, WA, USA",
        "denver" to "Denver, CO, USA",
        "washington" to "Washington, DC, USA",
        "boston" to "Boston, MA, USA",
        "el paso" to "El Paso, TX, USA",
        "nashville" to "Nashville, TN, USA",
        "detroit" to "Detroit, MI, USA",
        "oklahoma city" to "Oklahoma City, OK, USA",
        "portland" to "Portland, OR, USA",
        "las vegas" to "Las Vegas, NV, USA",
        "memphis" to "Memphis, TN, USA",
        "louisville" to "Louisville, KY, USA",
        "baltimore" to "Baltimore, MD, USA",
        "milwaukee" to "Milwaukee, WI, USA",
        "albuquerque" to "Albuquerque, NM, USA",
        "tucson" to "Tucson, AZ, USA",
        "fresno" to "Fresno, CA, USA",
        "sacramento" to "Sacramento, CA, USA",
        "mesa" to "Mesa, AZ, USA",
        "kansas city" to "Kansas City, MO, USA",
        "atlanta" to "Atlanta, GA, USA",
        "long beach" to "Long Beach, CA, USA",
        "colorado springs" to "Colorado Springs, CO, USA",
        "raleigh" to "Raleigh, NC, USA",
        "miami" to "Miami, FL, USA",
        "virginia beach" to "Virginia Beach, VA, USA",
        "omaha" to "Omaha, NE, USA",
        "oakland" to "Oakland, CA, USA",
        "minneapolis" to "Minneapolis, MN, USA",
        "tulsa" to "Tulsa, OK, USA",
        "arlington" to "Arlington, TX, USA",
        "tampa" to "Tampa, FL, USA",
        "new orleans" to "New Orleans, LA, USA",
        
        // International cities
        "london" to "London, UK",
        "paris" to "Paris, France",
        "tokyo" to "Tokyo, Japan",
        "sydney" to "Sydney, Australia",
        "melbourne" to "Melbourne, Australia",
        "berlin" to "Berlin, Germany",
        "madrid" to "Madrid, Spain",
        "rome" to "Rome, Italy",
        "amsterdam" to "Amsterdam, Netherlands",
        "zurich" to "Zurich, Switzerland",
        "vienna" to "Vienna, Austria",
        "prague" to "Prague, Czech Republic",
        "budapest" to "Budapest, Hungary",
        "warsaw" to "Warsaw, Poland",
        "moscow" to "Moscow, Russia",
        "beijing" to "Beijing, China",
        "shanghai" to "Shanghai, China",
        "hong kong" to "Hong Kong",
        "singapore" to "Singapore",
        "seoul" to "Seoul, South Korea",
        "mumbai" to "Mumbai, India",
        "delhi" to "Delhi, India",
        "bangalore" to "Bangalore, India",
        "dubai" to "Dubai, UAE",
        "cairo" to "Cairo, Egypt",
        "johannesburg" to "Johannesburg, South Africa",
        "nairobi" to "Nairobi, Kenya",
        "lagos" to "Lagos, Nigeria",
        "sao paulo" to "Sao Paulo, Brazil",
        "rio de janeiro" to "Rio de Janeiro, Brazil",
        "buenos aires" to "Buenos Aires, Argentina",
        "mexico city" to "Mexico City, Mexico",
        "toronto" to "Toronto, ON, Canada"
    )
    
    fun extractCityFromBio(bio: String): String {
        if (bio.isBlank()) return "Vancouver, BC, Canada"
        
        val bioLower = bio.lowercase()
        
        // Look for exact matches first
        for ((keyword, fullName) in cityKeywords) {
            if (bioLower.contains(keyword)) {
                return fullName
            }
        }
        
        // Look for partial matches (city name only)
        for ((keyword, fullName) in cityKeywords) {
            val cityName = keyword.split(" ")[0] // Get first word
            if (bioLower.contains(cityName) && cityName.length > 3) {
                return fullName
            }
        }
        
        // Default to Vancouver
        return "Vancouver, BC, Canada"
    }
    
    fun getCityCoordinates(cityName: String): Pair<Double, Double> {
        // Return coordinates for common cities
        return when {
            cityName.contains("Vancouver", ignoreCase = true) -> Pair(49.2827, -123.1207)
            cityName.contains("Toronto", ignoreCase = true) -> Pair(43.6532, -79.3832)
            cityName.contains("Montreal", ignoreCase = true) -> Pair(45.5017, -73.5673)
            cityName.contains("Calgary", ignoreCase = true) -> Pair(51.0447, -114.0719)
            cityName.contains("Ottawa", ignoreCase = true) -> Pair(45.4215, -75.6972)
            cityName.contains("Edmonton", ignoreCase = true) -> Pair(53.5461, -113.4938)
            cityName.contains("Winnipeg", ignoreCase = true) -> Pair(49.8951, -97.1384)
            cityName.contains("Quebec", ignoreCase = true) -> Pair(46.8139, -71.2080)
            cityName.contains("Hamilton", ignoreCase = true) -> Pair(43.2557, -79.8711)
            cityName.contains("London", ignoreCase = true) -> Pair(51.5074, -0.1278)
            cityName.contains("New York", ignoreCase = true) -> Pair(40.7128, -74.0060)
            cityName.contains("Los Angeles", ignoreCase = true) -> Pair(34.0522, -118.2437)
            cityName.contains("Chicago", ignoreCase = true) -> Pair(41.8781, -87.6298)
            cityName.contains("Houston", ignoreCase = true) -> Pair(29.7604, -95.3698)
            cityName.contains("Phoenix", ignoreCase = true) -> Pair(33.4484, -112.0740)
            cityName.contains("Philadelphia", ignoreCase = true) -> Pair(39.9526, -75.1652)
            cityName.contains("San Antonio", ignoreCase = true) -> Pair(29.4241, -98.4936)
            cityName.contains("San Diego", ignoreCase = true) -> Pair(32.7157, -117.1611)
            cityName.contains("Dallas", ignoreCase = true) -> Pair(32.7767, -96.7970)
            cityName.contains("San Jose", ignoreCase = true) -> Pair(37.3382, -121.8863)
            cityName.contains("Austin", ignoreCase = true) -> Pair(30.2672, -97.7431)
            cityName.contains("Jacksonville", ignoreCase = true) -> Pair(30.3322, -81.6557)
            cityName.contains("Fort Worth", ignoreCase = true) -> Pair(32.7555, -97.3308)
            cityName.contains("Columbus", ignoreCase = true) -> Pair(39.9612, -82.9988)
            cityName.contains("Charlotte", ignoreCase = true) -> Pair(35.2271, -80.8431)
            cityName.contains("Seattle", ignoreCase = true) -> Pair(47.6062, -122.3321)
            cityName.contains("Denver", ignoreCase = true) -> Pair(39.7392, -104.9903)
            cityName.contains("Washington", ignoreCase = true) -> Pair(38.9072, -77.0369)
            cityName.contains("Boston", ignoreCase = true) -> Pair(42.3601, -71.0589)
            cityName.contains("El Paso", ignoreCase = true) -> Pair(31.7619, -106.4850)
            cityName.contains("Nashville", ignoreCase = true) -> Pair(36.1627, -86.7816)
            cityName.contains("Detroit", ignoreCase = true) -> Pair(42.3314, -83.0458)
            cityName.contains("Oklahoma City", ignoreCase = true) -> Pair(35.4676, -97.5164)
            cityName.contains("Portland", ignoreCase = true) -> Pair(45.5152, -122.6784)
            cityName.contains("Las Vegas", ignoreCase = true) -> Pair(36.1699, -115.1398)
            cityName.contains("Memphis", ignoreCase = true) -> Pair(35.1495, -90.0490)
            cityName.contains("Louisville", ignoreCase = true) -> Pair(38.2527, -85.7585)
            cityName.contains("Baltimore", ignoreCase = true) -> Pair(39.2904, -76.6122)
            cityName.contains("Milwaukee", ignoreCase = true) -> Pair(43.0389, -87.9065)
            cityName.contains("Albuquerque", ignoreCase = true) -> Pair(35.0844, -106.6504)
            cityName.contains("Tucson", ignoreCase = true) -> Pair(32.2226, -110.9747)
            cityName.contains("Fresno", ignoreCase = true) -> Pair(36.7378, -119.7871)
            cityName.contains("Sacramento", ignoreCase = true) -> Pair(38.5816, -121.4944)
            cityName.contains("Mesa", ignoreCase = true) -> Pair(33.4152, -111.8315)
            cityName.contains("Kansas City", ignoreCase = true) -> Pair(39.0997, -94.5786)
            cityName.contains("Atlanta", ignoreCase = true) -> Pair(33.7490, -84.3880)
            cityName.contains("Long Beach", ignoreCase = true) -> Pair(33.7701, -118.1937)
            cityName.contains("Colorado Springs", ignoreCase = true) -> Pair(38.8339, -104.8214)
            cityName.contains("Raleigh", ignoreCase = true) -> Pair(35.7796, -78.6382)
            cityName.contains("Miami", ignoreCase = true) -> Pair(25.7617, -80.1918)
            cityName.contains("Virginia Beach", ignoreCase = true) -> Pair(36.8529, -75.9780)
            cityName.contains("Omaha", ignoreCase = true) -> Pair(41.2565, -95.9345)
            cityName.contains("Oakland", ignoreCase = true) -> Pair(37.8044, -122.2712)
            cityName.contains("Minneapolis", ignoreCase = true) -> Pair(44.9778, -93.2650)
            cityName.contains("Tulsa", ignoreCase = true) -> Pair(36.1540, -95.9928)
            cityName.contains("Arlington", ignoreCase = true) -> Pair(32.7357, -97.1081)
            cityName.contains("Tampa", ignoreCase = true) -> Pair(27.9506, -82.4572)
            cityName.contains("New Orleans", ignoreCase = true) -> Pair(29.9511, -90.0715)
            cityName.contains("Paris", ignoreCase = true) -> Pair(48.8566, 2.3522)
            cityName.contains("Tokyo", ignoreCase = true) -> Pair(35.6762, 139.6503)
            cityName.contains("Sydney", ignoreCase = true) -> Pair(-33.8688, 151.2093)
            cityName.contains("Melbourne", ignoreCase = true) -> Pair(-37.8136, 144.9631)
            cityName.contains("Berlin", ignoreCase = true) -> Pair(52.5200, 13.4050)
            cityName.contains("Madrid", ignoreCase = true) -> Pair(40.4168, -3.7038)
            cityName.contains("Rome", ignoreCase = true) -> Pair(41.9028, 12.4964)
            cityName.contains("Amsterdam", ignoreCase = true) -> Pair(52.3676, 4.9041)
            cityName.contains("Zurich", ignoreCase = true) -> Pair(47.3769, 8.5417)
            cityName.contains("Vienna", ignoreCase = true) -> Pair(48.2082, 16.3738)
            cityName.contains("Prague", ignoreCase = true) -> Pair(50.0755, 14.4378)
            cityName.contains("Budapest", ignoreCase = true) -> Pair(47.4979, 19.0402)
            cityName.contains("Warsaw", ignoreCase = true) -> Pair(52.2297, 21.0122)
            cityName.contains("Moscow", ignoreCase = true) -> Pair(55.7558, 37.6176)
            cityName.contains("Beijing", ignoreCase = true) -> Pair(39.9042, 116.4074)
            cityName.contains("Shanghai", ignoreCase = true) -> Pair(31.2304, 121.4737)
            cityName.contains("Hong Kong", ignoreCase = true) -> Pair(22.3193, 114.1694)
            cityName.contains("Singapore", ignoreCase = true) -> Pair(1.3521, 103.8198)
            cityName.contains("Seoul", ignoreCase = true) -> Pair(37.5665, 126.9780)
            cityName.contains("Mumbai", ignoreCase = true) -> Pair(19.0760, 72.8777)
            cityName.contains("Delhi", ignoreCase = true) -> Pair(28.7041, 77.1025)
            cityName.contains("Bangalore", ignoreCase = true) -> Pair(12.9716, 77.5946)
            cityName.contains("Dubai", ignoreCase = true) -> Pair(25.2048, 55.2708)
            cityName.contains("Cairo", ignoreCase = true) -> Pair(30.0444, 31.2357)
            cityName.contains("Johannesburg", ignoreCase = true) -> Pair(-26.2041, 28.0473)
            cityName.contains("Nairobi", ignoreCase = true) -> Pair(-1.2921, 36.8219)
            cityName.contains("Lagos", ignoreCase = true) -> Pair(6.5244, 3.3792)
            cityName.contains("Sao Paulo", ignoreCase = true) -> Pair(-23.5505, -46.6333)
            cityName.contains("Rio de Janeiro", ignoreCase = true) -> Pair(-22.9068, -43.1729)
            cityName.contains("Buenos Aires", ignoreCase = true) -> Pair(-34.6118, -58.3960)
            cityName.contains("Mexico City", ignoreCase = true) -> Pair(19.4326, -99.1332)
            else -> Pair(49.2827, -123.1207) // Default to Vancouver
        }
    }
}

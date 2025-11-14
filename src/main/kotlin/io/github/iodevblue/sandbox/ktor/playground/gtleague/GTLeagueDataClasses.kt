package io.github.iodevblue.sandbox.ktor.playground.gtleague

import kotlinx.serialization.Serializable


@Serializable
data class GlobalRanking(
    val rank: Int,
    val player: String,
    val globalPoints: Int,
    val points: Int,
    val winPercent: Double,
    val drawPercent: Double,
    val losePercent: Double,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val goalsForPerMatch: Double,
    val goalsAgainstPerMatch: Double,
    val pointsPerMatch: Double
)

@Serializable
data class Probabilities(val player1Win: Double, val player2Win: Double, val draw: Double)

@Serializable
data class MatchupResult(
    val player1: String,
    val player2: String,
    val probability: Probabilities,
    val recommendation: String,
    val expectedTotalGoals: Double,
    val expectedGoalsPlayer1: Double,
    val expectedGoalsPlayer2: Double,
    val expectedConcedePlayer1: Double,
    val expectedConcedePlayer2: Double,
    val player1Stats: GlobalRanking,
    val player2Stats: GlobalRanking
)


// Ranking provided by Gemini. Check https://www.gtleagues.com/global-ranking for updates.
val globalRankings = listOf(
    GlobalRanking(1, "Nio", 14486, 20372, 38.92, 23.87, 37.20, 5638, 3458, 5389, 33475, 31709, 1766, 2.31, 2.19, 1.41),
    GlobalRanking(2, "Jack", 11869, 17399, 41.52, 22.03, 36.41, 4928, 2615, 4322, 25908, 25168, 740, 2.18, 2.12, 1.47),
    GlobalRanking(3, "Professor", 12077, 15929, 36.65, 21.95, 41.38, 4426, 2651, 4998, 29607, 30772, -1165, 2.45, 2.55, 1.32),
    GlobalRanking(4, "David", 8672, 12709, 41.93, 20.77, 37.29, 3636, 1801, 3234, 22560, 21626, 934, 2.60, 2.49, 1.47),
    GlobalRanking(5, "Carlos", 8528, 12137, 41.02, 19.27, 39.72, 3498, 1643, 3387, 23539, 22965, 574, 2.76, 2.69, 1.42),
    GlobalRanking(6, "Shelby", 8087, 11213, 40.48, 17.20, 42.30, 3274, 1391, 3421, 21096, 21344, -248, 2.61, 2.64, 1.39),
    GlobalRanking(7, "Prince", 8410, 10669, 36.91, 16.14, 46.96, 3104, 1357, 3949, 24259, 27131, -2872, 2.88, 3.23, 1.27),
    GlobalRanking(8, "Arthur", 7565, 10525, 38.78, 22.78, 38.43, 2934, 1723, 2907, 16198, 15735, 463, 2.14, 2.08, 1.39),
    GlobalRanking(9, "Diego", 7065, 10328, 42.56, 18.50, 38.94, 3007, 1307, 2751, 24414, 23819, 595, 3.46, 3.37, 1.46),
    GlobalRanking(10, "Delpiero", 6788, 9500, 39.53, 21.38, 39.08, 2683, 1451, 2653, 15788, 15662, 126, 2.33, 2.31, 1.40),
    GlobalRanking(11, "Klaus", 5815, 8089, 38.90, 22.41, 38.69, 2262, 1303, 2250, 11581, 11641, -60, 1.99, 2.00, 1.39),
    GlobalRanking(12, "Lucas", 5712, 7808, 38.66, 20.73, 40.62, 2208, 1184, 2320, 13142, 13635, -493, 2.30, 2.39, 1.37),
    GlobalRanking(13, "Dempsey", 5180, 7514, 39.13, 27.66, 33.20, 2027, 1433, 1720, 9319, 8505, 814, 1.80, 1.64, 1.45),
    GlobalRanking(14, "Spartacus", 4795, 6858, 39.31, 25.09, 35.60, 1885, 1203, 1707, 9216, 8623, 593, 1.92, 1.80, 1.43),
    GlobalRanking(15, "Lio", 5120, 6749, 37.79, 18.44, 43.77, 1935, 944, 2241, 11351, 12353, -1002, 2.22, 2.41, 1.32),
    GlobalRanking(16, "Banega", 4457, 6462, 41.24, 21.27, 37.49, 1838, 948, 1671, 10724, 10688, 36, 2.41, 2.40, 1.45),
    GlobalRanking(17, "Zangief", 5444, 6343, 32.53, 18.92, 48.55, 1771, 1030, 2643, 11865, 14158, -2293, 2.18, 2.60, 1.17),
    GlobalRanking(18, "Vendetta", 4191, 5675, 37.72, 22.24, 40.04, 1581, 932, 1678, 7847, 7743, 104, 1.87, 1.85, 1.35),
    GlobalRanking(19, "Miguel", 3269, 5504, 48.42, 23.10, 28.48, 1583, 755, 931, 7636, 6351, 1285, 2.34, 1.94, 1.68),
    GlobalRanking(20, "Potter", 4105, 5401, 36.57, 21.88, 41.56, 1501, 898, 1706, 6939, 7285, -346, 1.69, 1.77, 1.32),
    GlobalRanking(21, "Hussein", 3246, 5196, 45.66, 23.11, 31.24, 1482, 750, 1014, 6948, 6053, 895, 2.14, 1.86, 1.60),
    GlobalRanking(22, "Sensei", 3394, 5024, 42.96, 19.15, 37.89, 1458, 650, 1286, 7757, 7152, 605, 2.29, 2.11, 1.48),
    GlobalRanking(23, "Jetli", 3285, 4456, 38.54, 20.03, 41.43, 1266, 658, 1361, 7062, 7262, -200, 2.15, 2.21, 1.36),
    GlobalRanking(24, "Giant", 2523, 4385, 51.61, 18.99, 29.41, 1302, 479, 742, 6399, 4800, 1599, 2.54, 1.90, 1.74),
    GlobalRanking(25, "Razvan", 3186, 3997, 35.88, 17.83, 46.30, 1143, 568, 1475, 6857, 7909, -1052, 2.15, 2.48, 1.25),
    GlobalRanking(26, "Eros", 1599, 3125, 58.16, 20.95, 20.89, 930, 335, 334, 3469, 2233, 1236, 2.17, 1.40, 1.95),
    GlobalRanking(27, "Viking", 1968, 2825, 40.85, 20.99, 38.16, 804, 413, 751, 3776, 3740, 36, 1.92, 1.90, 1.44),
    GlobalRanking(28, "Eminem", 1666, 2583, 46.58, 15.31, 38.12, 776, 255, 635, 4028, 3580, 448, 2.42, 2.15, 1.55),
    GlobalRanking(29, "Dusan", 1537, 2420, 44.96, 22.58, 32.47, 691, 347, 499, 3487, 3118, 369, 2.27, 2.03, 1.57),
    GlobalRanking(30, "Fred", 1513, 2240, 42.10, 21.74, 36.15, 637, 329, 547, 3269, 3156, 113, 2.16, 2.09, 1.48),
    GlobalRanking(31, "Cyclop", 1946, 2160, 33.14, 11.56, 55.29, 645, 225, 1076, 4048, 5105, -1057, 2.08, 2.62, 1.11),
    GlobalRanking(32, "Ryan", 1192, 2035, 52.10, 14.43, 33.47, 621, 172, 399, 3000, 2303, 697, 2.52, 1.93, 1.71),
    GlobalRanking(33, "Moby", 1203, 1522, 38.07, 12.30, 49.63, 458, 148, 597, 2783, 3113, -330, 2.31, 2.59, 1.27),
    GlobalRanking(34, "Hulk", 914, 1493, 48.91, 16.63, 34.46, 447, 152, 315, 2279, 1902, 377, 2.49, 2.08, 1.63),
    GlobalRanking(35, "Habibi", 812, 1078, 38.30, 17.86, 43.84, 311, 145, 356, 1631, 1807, -176, 2.01, 2.23, 1.33),
    GlobalRanking(36, "Leonidas", 652, 731, 32.06, 15.95, 51.99, 209, 104, 339, 1438, 1798, -360, 2.21, 2.76, 1.12),
    GlobalRanking(37, "Veron", 792, 591, 21.21, 10.98, 67.80, 168, 87, 537, 1604, 2540, -936, 2.03, 3.21, 0.75),
    GlobalRanking(38, "Snail", 374, 512, 38.77, 20.59, 40.64, 145, 77, 152, 807, 828, -21, 2.16, 2.21, 1.37),
    GlobalRanking(39, "Karim", 230, 391, 53.04, 10.87, 36.09, 122, 25, 83, 696, 662, 34, 3.03, 2.88, 1.70),
    GlobalRanking(40, "Mexican", 144, 366, 81.25, 10.42, 8.33, 117, 15, 12, 533, 240, 293, 3.70, 1.67, 2.54),
    GlobalRanking(41, "Crysis", 372, 348, 24.46, 20.16, 55.38, 91, 75, 206, 701, 953, -252, 1.88, 2.56, 0.94)
)
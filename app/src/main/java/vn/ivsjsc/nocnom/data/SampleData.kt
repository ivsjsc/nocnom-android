package vn.ivsjsc.nocnom.data

import vn.ivsjsc.nocnom.domain.model.*

object SampleData {
    val state = UserState(
        categories = listOf(
            Category("c1", "Món mặn"),
            Category("c2", "Món canh"),
            Category("c3", "Món nước"),
            Category("c4", "Ăn vặt & Đồ uống"),
            Category("c5", "Món chay"),
            Category("c6", "Tráng miệng"),
            Category("c7", "Combo / Phần ăn"),
        ),
        dishes = listOf(
            Dish(
                id = "d1",
                name = "Cơm gà xối mỡ",
                categoryId = "c1",
                isFavorite = true,
                imageUrl = "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?auto=format&fit=crop&w=800&q=80",
                nutrition = NutritionSnapshot(calories = 780.0),
            ),
            Dish(
                id = "d2",
                name = "Phở bò / Phở gà",
                categoryId = "c3",
                nutrition = NutritionSnapshot(calories = 520.0),
            ),
            Dish(
                id = "d3",
                name = "Trái cây + sữa chua",
                categoryId = "c6",
                nutrition = NutritionSnapshot(calories = 260.0, proteinG = 8.0, carbsG = 42.0, fatG = 6.0),
            ),
        ),
        profile = UserProfile(
            fullName = "Người dùng nOcnOm",
            dailyCalorieTarget = 2000,
            healthGoal = "maintain",
        ),
        source = DataSource.DEMO,
    )
}

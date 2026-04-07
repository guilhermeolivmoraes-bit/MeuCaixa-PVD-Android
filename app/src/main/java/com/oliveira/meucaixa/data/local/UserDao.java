package com.oliveira.meucaixa.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.oliveira.meucaixa.data.model.User;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Query("SELECT * FROM users WHERE full_name = :fullName AND birth_date = :birthDate LIMIT 1")
    User findUser(String fullName, String birthDate);

    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 1")
    LiveData<User> getLastUser();
}

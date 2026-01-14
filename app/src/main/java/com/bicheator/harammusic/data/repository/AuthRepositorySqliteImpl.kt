package com.bicheator.harammusic.data.repository

import android.database.sqlite.SQLiteDatabase
import com.bicheator.harammusic.core.result.AppResult
import com.bicheator.harammusic.core.session.SessionManager
import com.bicheator.harammusic.core.util.IdGenerator
import com.bicheator.harammusic.core.util.PasswordHasher
import com.bicheator.harammusic.domain.model.Role
import com.bicheator.harammusic.domain.model.User
import com.bicheator.harammusic.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositorySqliteImpl(
    private val sessionManager: SessionManager,
    private val dbProvider: () -> SQLiteDatabase
) : AuthRepository {

    override suspend fun login(username: String, password: String): AppResult<User> = withContext(Dispatchers.IO) {
        runCatching {
            val db = dbProvider()
            val c = db.rawQuery(
                """
                SELECT id, username, email, display_name, role, is_blocked, password_hash
                FROM users
                WHERE username = ?
                """.trimIndent(),
                arrayOf(username.trim())
            )

            c.use {
                if (!it.moveToFirst()) return@withContext AppResult.Error("Usuario o contraseña incorrectos")

                val storedHash = it.getString(6)
                val inputHash = PasswordHasher.sha256(password)
                if (storedHash != inputHash) return@withContext AppResult.Error("Usuario o contraseña incorrectos")

                val isBlocked = it.getInt(5) == 1
                if (isBlocked) return@withContext AppResult.Error("Usuario bloqueado")

                val user = User(
                    id = it.getString(0),
                    username = it.getString(1),
                    email = it.getString(2),
                    displayName = it.getString(3),
                    role = Role.valueOf(it.getString(4)),
                    isBlocked = isBlocked
                )
                sessionManager.setUser(user)
                AppResult.Success(user)
            }
        }.getOrElse { AppResult.Error("No se pudo iniciar sesión", it) }
    }

    override suspend fun register(username: String, email: String, password: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val u = username.trim()
            val e = email.trim()
            val now = System.currentTimeMillis()

            val db = dbProvider()

            val exists = db.rawQuery("SELECT 1 FROM users WHERE username = ? LIMIT 1", arrayOf(u))
                .use { it.moveToFirst() }
            if (exists) return@withContext AppResult.Error("Ese usuario ya existe")

            db.execSQL(
                """
                INSERT INTO users (id, username, password_hash, email, display_name, role, is_blocked, created_at)
                VALUES (?, ?, ?, ?, ?, 'USER', 0, ?)
                """.trimIndent(),
                arrayOf(
                    IdGenerator.uuid(),
                    u,
                    PasswordHasher.sha256(password),
                    e,
                    u,
                    now
                )
            )

            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error("No se pudo registrar", it) }
    }

    override suspend fun logout(): AppResult<Unit> = withContext(Dispatchers.IO) {
        sessionManager.clear()
        AppResult.Success(Unit)
    }
}
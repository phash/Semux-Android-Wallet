/*
 * MIT License
 *
 * Copyright (c) 2018 Manuel Roedig / Phash
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.phash.manuel.asw.util

import android.content.Context
import org.mindrot.jbcrypt.BCrypt

private val NOKEY = "nokey"
private val PASSWORD_KEY = "password"

fun getCurrentPassword(context: Context): String? {
    val prefs = context.getSharedPreferences("de.phash.manuel.asw", Context.MODE_PRIVATE)
    return prefs.getString(PASSWORD_KEY, NOKEY)
}

fun isPasswordSet(context: Context): Boolean {
    return !getCurrentPassword(context).equals(NOKEY)
}

fun persistNewPassword(context: Context, passwordToSet: String) {
        persistPassword(context, passwordToSet)
}

fun persistPassword(context: Context, passwordToSet: String) {
    val salt = BCrypt.gensalt(12)
    context.getSharedPreferences("de.phash.manuel.asw", Context.MODE_PRIVATE).edit().putString(
            PASSWORD_KEY,
            BCrypt.hashpw(passwordToSet, salt)
    ).apply()
}


fun isPasswordCorrect(context: Context, passwordToTest: String): Boolean {
    return checkPassword(context, passwordToTest)
}

fun checkPassword(context: Context, passwordToTest: String): Boolean {
    return BCrypt.checkpw(passwordToTest, getCurrentPassword(context))

}

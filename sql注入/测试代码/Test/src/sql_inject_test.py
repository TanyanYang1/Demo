import sqlite3
import psycopg2
import mysql.connector

# 模拟用户输入
user_id = "1"
user_name = "test"

# 1. cursor.execute + % 格式化（SELECT/INSERT）
# SQLite 示例
conn_sqlite = sqlite3.connect("test.db")
cursor_sqlite = conn_sqlite.cursor()
cursor_sqlite.execute("SELECT * FROM user WHERE id = %s" % user_id)  # 触发 SELECT + % 规则
cursor_sqlite.execute("INSERT INTO user(name) VALUES ('%s')" % user_name)  # 触发 INSERT + % 规则

# 2. cursor.execute + 字符串拼接（UPDATE/DELETE）
cursor_sqlite.execute("UPDATE user SET age = 20 WHERE id = " + user_id)  # 触发 UPDATE + 拼接 规则
cursor_sqlite.execute("DELETE FROM user WHERE name = '" + user_name + "'")  # 触发 DELETE + 拼接 规则

# 3. sqlite3 + f-string 拼接
cursor_sqlite.execute(f"SELECT * FROM user WHERE id = {user_id}")  # 触发 sqlite3 + f-string 规则

# 4. psycopg2（PostgreSQL） + % 格式化
conn_pg = psycopg2.connect(database="test", user="postgres", password="123456", host="localhost")
cursor_pg = conn_pg.cursor()
cursor_pg.execute("SELECT * FROM user WHERE id = %s" % user_id)  # 触发 psycopg2 + SELECT + % 规则

# 5. mysql.connector + f-string
conn_mysql = mysql.connector.connect(host="localhost", user="root", password="123456", database="test")
cursor_mysql = conn_mysql.cursor()
cursor_mysql.execute(f"SELECT * FROM user WHERE name = '{user_name}'")  # 触发 mysql.connector + f-string 规则

# 关闭连接
cursor_sqlite.close()
conn_sqlite.close()
cursor_pg.close()
conn_pg.close()
cursor_mysql.close()
conn_mysql.close()
package main

import (
    "database/sql"
    "fmt"
    _ "github.com/go-sql-driver/mysql"
)

func main() {
    // 模拟用户输入
    userId := "1"
    userName := "test"

    // 1. fmt.Sprintf 拼接 SELECT/INSERT
    sql1 := fmt.Sprintf("SELECT * FROM user WHERE id = %s", userId) // 触发 fmt.Sprintf(SELECT) 规则
    sql2 := fmt.Sprintf("INSERT INTO user(name) VALUES ('%s')", userName) // 触发 fmt.Sprintf(INSERT) 规则

    // 2. db.Exec/db.Query + fmt.Sprintf
    db, _ := sql.Open("mysql", "root:123456@tcp(localhost:3306)/test")
    db.Exec(fmt.Sprintf("UPDATE user SET age = 20 WHERE id = %s", userId)) // 触发 db.Exec + fmt.Sprintf 规则
    db.Query(fmt.Sprintf("DELETE FROM user WHERE id = %s", userId)) // 触发 db.Query + fmt.Sprintf 规则

    // 3. sql.DB.Query 字符串拼接
    db.Query("SELECT * FROM user WHERE name = '" + userName + "'") // 触发 sql.DB.Query + 拼接 规则

    defer db.Close()
}
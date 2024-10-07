### Параметры для запуска сервера
**webdav.folder** 

Минимальный конфиг для запуска `webdav.folder.path` (путь до папки)
```yaml
webdav.folder.path: '/home/${user.name}/webdav'
```
При необходимости можно указать логин и пароль, и host(header) c которого пришел запрос. 
Таким образом можно открыть доступ на разные каталоги в зависимости от Host: Domain.ru
```yaml
webdav.folder.login: ivanov
webdav.folder.password: 123456
webdav.folder.host: localhost:8080
```
Так же можно указать список каталогов для доступа `webdav.folders`. 
Имеет ту же структуру как при одиночной настройке каталога(`webdav.folder`) и объединяются при старте в единый список.
```yaml
webdav.folders[0].path: '/home/alekcei/webdav'
webdav.folders[0].login: ivanov
webdav.folders[0].password: 123456
webdav.folders[0].host: localhost:8080
```
При получении запроса на сервер с начало происходит поиск настроек из общего списка (`webdav.folder + webdav.folders`). 
На основании логина и host-header. После проходит проверка пароля если он задан на каталог.
Далее работа производится от найденных настроек  

**webdav.cors.origins**  
Включение cors запросов из браузера, по умолчанию отключено.  
Позволяет браузерному front делать запросы в webdav. 
см. проект `webdav-ui`
```yaml
webdav.cors.origins: '*'
# webdav.cors.origins: http://localhost:4200
```
**http.port**  
Порт для переадресовки с http на httpS, при запуске c `server.ssl.enabled: false` параметр не нужен. 
```yaml
http.port: 8082
```

### Проверка работы через сторонние существующие клиенты
Клиенты:
 - windows (сетевой диск в "Мой компьютер")
 - linux davfs(монтирование диска на linux)
 - linux подключение через проводник(nautilus, caja)
 - Cx проводник(android приложение)

### Запуск на Linux SystemMD
Пример минимальных настроек  
```properties
[Unit]
Description=WebDav Service
After=network-online.target

[Service]
WorkingDirectory=/opt/webdav/
MemoryHigh=256M
ExecStart=/usr/bin/java -Xmx256m -jar webdav.jar --webdav.folder.path=/home/alekcei --server.port=8080
User=alekcei
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```
**Команды для работы с сервисом**  
`systemctl daemon-reload`  - Применение настроек после редактирования  
`systemctl start webdav`  - Запуск сервиса  
`systemctl stop  webdav`  - Остановка  
`systemctl status webdav` - Проверка статуса сервиса  
`systemctl enable webdav` - Запуск при загрузке системы  
**Ссылки по настройке**  
https://prudnitskiy.pro/post/2018-01-24-systemd-quickstart/  
https://ru.linux-console.net/?p=8202  
https://habr.com/ru/companies/slurm/articles/255845/

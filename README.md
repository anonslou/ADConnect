# ADConnect

Простая реализация коннектора к Active Directory на Java (JNDI)

## Особенности

1. Поддержка ssl по сертификатам
2. Поиск объекта по имени
3. Получение/установка атрибутов объекта
4. Функции: создания пользователя, смены пароля пользователя, добавления пользователя в группу
5. Примеры использования см. в simpleDemos и в guiDemo

## guiDemo

Графическая утилита, которая строит таблицу из выбранных атрибутов у объектов, полученных через ldap фильтр. 

Интерфейс guiDemo:
![Интерфейс guiDemo](https://raw.githubusercontent.com/anonslou/ADConnect/master/ADConnect.png "интерфейс guiDemo")

Особенности:
1. Возможность экспорта полученных данных в csv.
2. Поддержка виртуальных атрибутов ip и ping.

Недостатки:
1. Атрибут lastlogon собирается только с двух контроллеров, а не со всех.
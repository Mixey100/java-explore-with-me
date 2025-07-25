# java-explore-with-me

Приложение для добавления и поиска развлекательных мероприятий. Использован следующий стек: Spring Boot, JPA, Hibernate, Postgres.

## Дополнительная информация

### Эндпоинты:

#### Пользователи:
Закрытый API:
- GET /admin/users - Получение информации о пользователях
- POST /admin/users - Добавление нового пользователя
- DELETE /admin/users/{userId} - Удаление пользователя

#### Категории:
Публичный API:
- GET /categories - Получение категорий
- GET /categories/{catId} - Получение информации о категории по её идентификатору

Закрытый API:
- POST /admin/categories - Добавление новой категории
- DELETE /admin/categories/{catId} - Удаление категории
- PATCH /admin/categories/{catId} - Изменение категории

#### События:
Публичный API:
- GET /events - Получение событий с возможностью фильтрации
- GET /events/{id} - Получение подробной информации об опубликованном событии по его идентификатору

Закрытый API:
- GET /users/{userId}/events - Получение событий, добавленных текущим пользователем
- GET /users/{userId}/events/{eventId} - Получение полной информации о событии добавленном текущим пользователем
- GET /users/{userId}/events/{eventId}/requests - Получение информации о запросах на участие в событии текущего пользователя

- POST /users/{userId}/events - Добавление нового события
- PATCH /users/{userId}/events/{eventId} - Изменение события добавленного текущим пользователем
- PATCH /users/{userId}/events/{eventId}/requests - Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя

- GET /admin/events - Поиск событий
- PATCH /admin/events/{eventId} - Редактирование данных события и его статуса (отклонение/публикация).

#### Подборки событий:
Публичный API:
- GET /compilations - Получение подборок событий
- GET /compilations/{compId} - Получение подборки событий по его id

Закрытый API:
- POST /admin/compilations - Добавление новой подборки (подборка может не содержать событий)
- DELETE /admin/compilations/{compId} - Удаление подборки
- PATCH /admin/compilations/{compId} - Обновить информацию о подборке

#### Запросы на участие:
Закрытый API:
- GET /users/{userId}/requests - Получение информации о заявках текущего пользователя на участие в чужих событиях
- POST /users/{userId}/requests - Добавление запроса от текущего пользователя на участие в событии
- PATCH /users/{userId}/requests/{requestId}/cancel - Отмена своего запроса на участие в событии

### Ссылка на PR: https://github.com/Mixey100/java-explore-with-me/pull/4
# Archeus – Fullstack Chat & Profilový systém 🛠️

**Archeus** je fullstack projekt (Spring Boot + React.js + Socket.IO), ktorý umožňuje:
- ✅ Prihlasovanie a registráciu používateľa (s overovacím e‑mailom)
- 🛡️ JWT autentifikáciu (access + refresh token)
- 💬 Real‑time chat medzi užívateľmi (Socket.IO)
- 👤 Editáciu profilových údajov + zmenu hesla
- 📧 Odosielanie e‑mailov (Yahoo SMTP)
- ☁️ Správu prostredia (env súbor)
- ⚙️ MySQL databázu

---

## 📁 Štruktúra projektu
├── backendSocket/ – Node.js server pre Socket.IO
├── my-frontend/ – React frontend
├── src/main/java/... – Java Spring Boot backend
├── src/main/resources/ – config + env/vlastnosti
├── uploads/ – uploadované súbory (napr. avatare)
├── pom.xml – Maven konfigurácia
└── .gitignore – ignorované súbory/generátory

## ⚙️ Inštalácia & spustenie

1. Skopíruj `.env.example` → `.env` (alebo `application-local.properties`):
   👇 Pevne pridaj:
   DB_URL=jdbc:mysql://localhost:3307/archeus?useSSL=false&...
DB_USER=archeus_user
DB_PASSWORD=pokemon23
MAIL_USER=6jetam@centrum.sk
MAIL_PASSWORD=pokemon
JWT_SECRET=mojevelmitajomneh3slo123456789012345

2. **Backend**:
```bash
mvn clean install
mvn spring-boot:run

Backend beží na http://localhost:8085.

Node.js Socket server (ak už máte samostatný):

cd backendSocket
npm install
node server.js

Socket.IO beží na http://localhost:5000.

Frontend:

cd my-frontend
npm install
npm start

React beží na http://localhost:3000.

Hlavné technológie

    Backend: Spring Boot, Spring Security, JWT, Spring Data JPA (MySQL), JavaMailSender

    Frontend: React 19, Axios, TailwindCSS, React Router, React Toastify

    Real‑time chat: Socket.IO (Node.js server & React klient)

    Database: MySQL

    Env: .env + dotenv-spring-boot (priorita), alebo application-local.properties

🛠️ Základné endpointy

    POST /api/users/register – registrácia + overovací e‑mail

    POST /api/users/login – prihlásenie, JWT access + refresh

    GET /api/users/me – načítanie prihláseného používateľa (s JWT)

    PUT /api/users/updateUser – úprava profilových údajov

    POST /api/change-password – zmena hesla

    GET /api/messages/:me/:other – získanie chatu medzi dvoma ID

    POST /api/messages/:id – upraviť správu

    DELETE /api/messages/:id – zmazať správu

    Socket: user_connected, send_message, receive_message, online_users

// server.js
const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
require('dotenv').config();
const socketHandler = require('./socket');

const app = express();
const server = http.createServer(app);

// 🔧 OPRAVA: povolený frontend port 3000
const io = socketIo(server, {
  cors: {
    origin: 'http://localhost:3000',
    methods: ['GET', 'POST'],
  },
});

app.use(cors());
app.use(express.json());

app.get('/', (req, res) => {
  res.send('✅ Chat backend beží!');
});

// 💬 Socket logika
socketHandler(io);

const PORT = process.env.PORT || 5000;
server.listen(PORT, () => {
  console.log(`🚀 Server beží na http://localhost:${PORT}`);
});

const onlineUsers = new Map();

io.on("connection", (socket) => {
  console.log("🔌 New socket connected:", socket.id);

  socket.on("user_connected", (userId) => {
    onlineUsers.set(userId, socket.id);
    console.log("✅ User connected:", userId);
    io.emit("online_users", Array.from(onlineUsers.keys())); // ⬅️ broadcast
  });

  socket.on("disconnect", () => {
    const disconnectedUser = [...onlineUsers.entries()]
      .find(([_, id]) => id === socket.id);

    if (disconnectedUser) {
      onlineUsers.delete(disconnectedUser[0]);
      console.log("❌ User disconnected:", disconnectedUser[0]);
      io.emit("online_users", Array.from(onlineUsers.keys()));
    }
  });
});


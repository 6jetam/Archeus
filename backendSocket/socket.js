// socket.js
const pool = require('./db');

const onlineUsers = new Map();

function socketHandler(io) {
  io.on('connection', (socket) => {
    console.log(`🔌 Socket pripojený (${socket.id})`);
    socket.userId = null;

    for (const [uid, socketSet] of onlineUsers.entries()) {
      if (socketSet.has(socket.id)) {
        socketSet.delete(socket.id);
        if (socketSet.size === 0) onlineUsers.delete(uid);
      }
    }

    socket.onAny((event, ...args) => {
  console.log(`📡 [${socket.id}] Event '${event}'`, args);
    });

    socket.on('user_connected', (userId) => {
      userId = Number(userId);
      socket.userId = userId;

      if (!onlineUsers.has(userId)) {
        onlineUsers.set(userId, new Set());
        console.log(`✅ Používateľ ${userId} prihlásený`);
      }

      onlineUsers.get(userId).add(socket.id);
      io.emit("online_users", Array.from(onlineUsers.keys()).map(Number));

    });

    socket.on('send_message', async ({ senderId, receiverId, content }) => {
  senderId = Number(senderId);
  receiverId = Number(receiverId);

  try {
    console.log(`✉️  Správa od ${senderId} → ${receiverId}: "${content}"`);

    const [result] = await pool.query(
      'INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)',
      [senderId, receiverId, content]
    );

    const messageId = result.insertId;

    const messagePayload = {
      id: messageId,                 // 🆕 id od DB
      senderId,
      receiverId,
      content,
      createdAt: new Date(),        // 🕒 aby to frontend mohol zobraziť
    };

    // 🔁 Príjemca
    const receiverSockets = onlineUsers.get(receiverId);
    if (receiverSockets) {
      for (const sockId of receiverSockets) {
        io.to(sockId).emit('receive_message', { ...messagePayload, incoming: true });
      }
    }

    // 🧑‍💻 Odosielateľ
    const senderSockets = onlineUsers.get(senderId);
    if (senderSockets) {
      for (const sockId of senderSockets) {
        io.to(sockId).emit('receive_message', { ...messagePayload, incoming: false });
      }
    }

  } catch (err) {
    console.error('❌ Chyba pri ukladaní správy:', err);
  }
});

    socket.on('disconnect', () => {
      const userId = socket.userId;
      console.log(`🔴 Socket odpojený (${socket.id} - userId: ${userId})`);

      if (userId && onlineUsers.has(userId)) {
        const userSockets = onlineUsers.get(userId);
        userSockets.delete(socket.id);

        if (userSockets.size === 0) {
          onlineUsers.delete(userId);
          console.log(`🔕 Používateľ ${userId} úplne odhlásený`);
          io.emit("online_users", Array.from(onlineUsers.keys()).map(Number));

        }
      }
    });
  });
}

module.exports = socketHandler;

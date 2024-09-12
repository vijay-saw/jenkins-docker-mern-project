const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();
const port = process.env.PORT || 5000;

// Middleware
// app.use(cors({
//   origin: ['http://localhost:3000', 'http://localhost:3001'], // Update this to your frontend's URL if different
//   methods: 'GET,POST',
//   allowedHeaders: 'Content-Type'
// }));

app.use(cors({
  origin: ['http://frontend-service:80'], // Update this to match your setup
  methods: 'GET,POST',
  allowedHeaders: 'Content-Type'
}));

app.use(bodyParser.json());

// MongoDB connection string
const mongoURI = 'mongodb+srv://vijaysaw50:vijay123@cluster0.ildzm.mongodb.net/mernapp?retryWrites=true&w=majority';

// Connect to MongoDB
mongoose.connect(mongoURI, { useNewUrlParser: true, useUnifiedTopology: true })
  .then(() => console.log('MongoDB connected'))
  .catch(err => console.log('MongoDB connection error:', err));

// Define a schema and model
const userSchema = new mongoose.Schema({
  name: String,
  phone: String,
  email: String
});

const User = mongoose.model('User', userSchema);

// POST endpoint to add a user
app.post('/users', async (req, res) => {
  try {
    const newUser = new User({
      name: req.body.name,
      phone: req.body.phone,
      email: req.body.email
    });
    const savedUser = await newUser.save();
    res.status(201).json(savedUser);
  } catch (err) {
    console.error('Error saving user:', err);
    res.status(500).json({ message: 'Internal Server Error', error: err.message });
  }
});

// GET endpoint to verify the server is running
app.get('/', (req, res) => {
  res.send('Server is up and running');
});

// Optional: Add a health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'UP' });
});

app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});

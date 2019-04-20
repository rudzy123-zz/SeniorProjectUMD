var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var flashSchema = new Schema({
	question: {type: String, required: true},
	answer: {type: String, required: true},
	author: {type: String, required: true}},
	{collection: 'flashQuestions'});

var questionsAdded = mongoose.model('questionsAdded', flashSchema);
module.exports = questionsAdded;

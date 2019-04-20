//Express-Session, Mongoose, and body-parser declarations
var express = require('express');
var app = express();
var mongoose = require('mongoose');
var session = require('express-session');
var bp = require('body-parser');



//Load routing data to launch local copy of server from routing.json
const fs = require('fs');
var route = fs.readFileSync('test.json');
var jsonRoute = JSON.parse(route);

//connecting mongoose to RestAPI, target URL stored in route
mongoose.connect('mongodb://ukko.d.umn.edu:42222/AppNull');
var db = mongoose.connection

//handles mongo connection error
db.on('error', console.error.bind(console, 'connection error:'));
db.once('open', function () {
  console.log("Connection established to" + "mongodb://ukko.d.umn.edu:42222/AppNull");
});

// Mongoose schema declartion
var technical = require('./models/Technical.js');
var flash = require('./models/flash.js')
var User = require('./models/User.js')
var AddedQuestion = require('./models/myDB.js')

// set up for pug enginer
app.set('views', './views'); //folder where views are stored
app.set('view engine', 'pug');

//use session for tracking login
app.use(session({
  secret: 'hardly technical',
  resave: true,
  saveUninitialized: false
}));

app.use(bp.json());
app.use(bp.urlencoded({ extended: false }));

console.log("\n Starting Server, set-up complete \n");

/**
 * Basic .get that sends a simple hello message on connection
 * @param req 	the request sent to the server
 * @param res 	the response sent to the client
 * @return	sends a "hello" message to the server console
 */
app.get('/', (req, res) => {
	console.log("HELLO");
	res.render('home');
});

/**
 * Register command to create a new user from question-website homepage
 * @param req   the request sent to the server
 * @param next   used to handle error responses
 * @param res   the response sent to the client; must contain email, username, password, and passwordConf fields, fields must not be empty
 * @return  redirects user to profile on succesful creation, spits out error on error
 */
app.post('/register', function (req, res, next) {
  // confirm that user typed same password twice
  if (req.body.password !== req.body.passwordConf) {
  	console.log('passwords dont match');
    var err = new Error('Passwords do not match.');
    err.status = 400;
    res.send("passwords dont match");
    return next(err);
  }

  if (req.body.email &&
    req.body.username &&
    req.body.password &&
    req.body.passwordConf && (req.body.verificationCode == "Questioneers")) {
    
    console.log('creating new user');
    var userData = {
      email: req.body.email,
      username: req.body.username,
      password: req.body.password,
      passwordConf: req.body.passwordConf,
      accessLevel: "admin",
    }

    User.create(userData, function (error, user) {
      if (error) {
        return next(error);
      } else {
        req.session.userId = user._id;
        return res.redirect('/profile');
      }
    });

  } else  {
    var err = new Error('All fields required.');
    err.status = 400;
    return next(err);
  }
});

/**
 * profile call, redirects to profile page
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @param next   used to handle error responses
 * @return  renders the profile page with the username, email, and access level of user
 */
app.get('/profile', function (req, res, next) {
  User.findById(req.session.userId)
    .exec(function (error, user) {
      if (error) {
        return next(error);
      } else {
        if (user === null) {
          var err = new Error('Not authorized! Go back!');
          err.status = 400;
          return next(err);
        } else {
          return res.render('profile', { user : user.username, email : user.email, access : user.accessLevel })
        }
      }
    });
});

/**
 * updates flashcard question page to list questions in a bank
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @param next   used to handle error responses
 * @return  renders flashcard question page
 */
app.post('/getOnlineQuestions', function (req, res, next) {
  User.findById(req.session.userId)
    .exec(function (error, user) {
        var questions;
      if (error) {
        return next(error);
      } else {
        if (user === null) {
          var err = new Error('Not authorized! Go back!');
          err.status = 400;
          return next(err);
        } else {
                 db.collection(req.body.bank).find({}).toArray(function(err, data) {
        if (err) console.log(err);
        else{
                 var bankNames = db.db.listCollections().toArray(function(err, names){
                        var bank = new Array(names.length);
                        var i = 1;
                        names.forEach(function(element) {
                                if( element.name != "users" )
                                {
                                        bank[i] = element.name;
                                        i++;
                                }
                        bank[0] = "SELECT A BANK";
                        });
                var ques = new Array(data.length);
                i=0;
                data.forEach(function(element) {
                        ques[i] = element.question;
                        i++;
                });
                if( ques.length == 0)
                        ques[0] = "ERROR: BANK IS EMPTY";
                return res.render('addFlash',{names: bank, questions: ques, bank: req.body.bank});
        });
    }
});
      }
    }
});
});

/**
 * redirects to create a flashcard question page
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @param next   used to handle error responses
 * @return  renders flashcard question page
 */
app.get('/makeFlash', function (req, res, next) {
  User.findById(req.session.userId)
    .exec(function (error, user) {
      if (error) {
        return next(error);
      } else {
        if (user === null) {
          var err = new Error('Not authorized! Go back!');
          err.status = 400;
          return next(err);
        } else {
                 var bankNames = db.db.listCollections().toArray(function(err, names){
                        var bank = new Array(names.length);
                        var i = 1;
                        names.forEach(function(element) {
                                if( element.name != "users" )
                                {
                                        bank[i] = element.name;
                                        i++;
                                }
			bank[0] = "SELECT A BANK";
                        });
	var ques = {};
        return res.render('addFlash', {names: bank, questions: ques} );
        });
}
      }
    });
});



/**
 * redirects to create a technical question page
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @param next   used to handle error responses
 * @return  renders technical question page
 */
app.get('/makeTechnical', function (req, res, next) {
  User.findById(req.session.userId)
    .exec(function (error, user) {
      if (error) {
        return next(error);
      } else {
        if (user === null) {
          var err = new Error('Not authorized! Go back!');
          err.status = 400;
          return next(err);
        } else {
          return res.render('addTechnical', { user : user.username, email : user.email, access : user.accessLevel })
        }
      }
    });
});

/**
 * redirects to creat flashcard question page
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @param next   used to handle error responses
 * @return  renders flashcard question page
 */
app.get('/logout', function (req, res, next) {
  if (req.session) {
    // delete session object
    req.session.destroy(function (err) {
      if (err) {
        return next(err);
      } else {
        return res.redirect('/');
      }
    });
  }
});

/** used to retrieve a random technical question from the technical question database
 * @param req 	the request sent to the server
 * @param res 	the response sent to the client
 * @return 	sends a random technical question as a string back to the client
 */
app.get('/getTechnical', (req, res) => {
  console.log("getTech called...");
  technical.findOneRandom(function(err, doc) {
       if(err) console.log(err);
else {
       console.log("Question", doc.question)
       res.send(doc.question);}
     });

});

/** used to retrieve all questions from the flashcard question database
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @return  sends a random flashcard question as a string back to the client
 */

app.get('/getAll', (req, res) => {
	AddedQuestion.find({}, function(err, data) { 
		if (err) console.log(err);
		else{
		console.log(data.question); 
		res.send(data);
		}
	});
});


/** used to retrieve a random flashcard question from the flashcard question database
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @return  sends a random flashcard question as a string back to the client
 */
app.post('/getFlash', (req, res) => {
	console.log("getFlash called...");
	db.db.collection(req.body.bankName).aggregate([{ $sample: { size: 1 } }]).next(function(err, doc) {
		if (err) console.log(err);
		else {
			console.log(doc.question);
			console.log(doc.answer);
			res.send(doc);
		}
	});
});

/** login call
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @param next   used to handle error responses
 * @return  succesful login redirects the user to the profile page
 */
app.post('/login', function (req, res, next) {
	if (req.body.loguser && req.body.logpassword) {
    User.authenticate(req.body.loguser, req.body.logpassword, function (error, user) {
      if (error || !user) {
        var err = new Error('Wrong email or password.');
        err.status = 401;
        return next(err);
      } else {
        req.session.userId = user._id;
        return res.redirect('/profile');
      }
    });
  } else  {
    var err = new Error('All fields required.');
    err.status = 400;
    return next(err);
  }
});

/** sends a newly created flashcard question to the database from the website
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @param next   used to handle error responses
 * @return  after sending the question, it redirects to the profile page
 */
app.post('/createFlash', function (req, res, next) {
User.findById(req.session.userId)
    .exec(function (error, user) {
      if (error) {
        return next(error + "hello");
      } else {
        if (user === null) {
          var err = new Error('Not authorized! Go back!');
          err.status = 400;
          return next(err);
        } else if(req.body.question === "" || req.body.answer === "") {
                var err = new Error('Field blank');
                err.status = 400;
                return next(err + "blank");
        } else {
                      var question = {
                              question: req.body.question,
                              answer: req.body.answer,
                              author: req.session.userId
                    }
                db.collection(req.body.bank).insert(question, function (error, user) {
                              if (error) {
                                return next(error + "bye");
                              }
                            });
          return res.render('profile', { user : user.username, email : user.email, access : user.accessLevel })
        }
      }
    });
});

app.post('/makeFlash', function (req, res, next) {
user = db.collection('users').findOne({username : req.body.username }).then(function(user){
        if (user === null) {
          var err = new Error('Not authorized! Go back!');
          err.status = 400;
          return next(err);
        } else if(req.body.question === "" || req.body.answer === "") {
                var err = new Error('Field blank');
                err.status = 400;
                return next(err + "blank");
        } else {
                      var question = {
                              question: req.body.question,
                              answer: req.body.answer,
                              author: req.body.username
                }
                db.collection(req.body.bank).insert(question, function (error, user) {
                              if (error) {
                                return next(error + "bye");
                              }
                            });
        return res.send({ user : user.username, email : user.email, access : user.accessLevel })
        }
      });
});
/** deleteQuestion call
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @param next   used to handle error responses
 * @return  succesful deletion of question in the data base.
 */
app.post('/deleteQuestion', function (req, res, next) {
User.findById(req.session.userId)
    .exec(function (error, user) {
      if (error) {
        return next(error);
      } else {
                if(user === null){
                        req.session.userId = null;
                        user = db.collection('users').findOne({ username: req.body.username })
                }
        if (user === null) {
          var err = new Error('Not authorized! Go back!');
          err.status = 400;
          return next(err);
        } else if(req.body.question === "" || req.body.answer === "") {
                var err = new Error('Field blank');
                err.status = 400;
                return next(err);
        } else {
                var question = {
                              question: req.body.question,
                }
                console.log(req.body.bank);
                db.collection(req.body.bank).findOneAndDelete(question, function (error, user) {
                              if (error) {
                                return next(error + "bye");
                              }
                            });
                console.log("test");
                 if(req.session.userId !== null)
		{
                         return res.render('profile', { user : user.username, email : user.email, access : user.accessLevel});
		}
                else
		{
                         return res.send({ user : user.username, email : user.email, access : user.accessLevel})
		 }
	}
      }
    });
});


/** sends a newly created technical question to the database from the website
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @param next   used to handle error responses
 * @return  after sending the question, it redirects to the profile page
 */
app.post('/createTechnical', function (req, res, next) {
  User.findById(req.session.userId)
    .exec(function (error, user) {
      if (error) {
        return next(error);
      } else {
        if (user === null) {
          var err = new Error('Not authorized! Go back!');
          err.status = 400;
          return next(err);
        } else if(req.body.question === "") {
        	var err = new Error('Field blank');
          	err.status = 400;
         	return next(err);
        } else {
		      var question = {
			      question: req.body.question,
			      author: req.session.userId
		    }
        	technical.create(question, function (error, user) {
			      if (error) {
			        return next(error);
			      }
			    });
          return res.render('profile', { user : user.username, email : user.email, access : user.accessLevel })
        }
      }
    });
});

/** Creates new empty bank of questions (a collection)
 * @param req   the request sent to the server
 * @param res   the response sent to the client
 * @param next   used to handle error responses
 * @return  after sending the question, it redirects to the profile page
 */
app.post('/createBank', function(req, res, next){
   user = db.collection('users').findOne({ username: req.body.username }).then(function (user) {
      if (user != null) {
        if (user === null) {
          var err = new Error('Not authorized! Go back!');
          err.status = 400;
          return next(err);
        } else if(req.body.bankName === "") {
            var err = new Error('Field blank');
            err.status = 400;
            return next(err);
        } else {
              db.createCollection(req.body.bankName, function(err){
            if(err)
              return next(err);
          });
          return res.send({ user : user.username, email : user.email, access : user.accessLevel, bankName: req.body.bankName })
        }
      }
    });
});
app.post('/createBankonline', function (req, res, next) {
User.findById(req.session.userId)
    .exec(function (error, user) {
      if (error) {
        return next(error);
      } else {
        if (user === null) {
          var err = new Error('Not authorized! Go back!');
          err.status = 400;
          return next(err);
        } else if(req.body.bankName === "") {
                var err = new Error('Field blank');
                err.status = 400;
                return next(err);
        } else {
              db.createCollection(req.body.bankName, function(err){
            if(err)
              return next(err);
        });
        return res.render('profile', { user : user.username, email : user.email, access : user.accessLevel});
      }
    }});
});



app.post('/deleteBank', function(req, res){
User.findById(req.session.userId)
    .exec(function (error, user) {
      if (error) {
        return next(error);
      } else {
                if(user === null){
                        req.session.userId = null;
                        user = db.collection('users').findOne({ username: req.body.username })
                }
        if (user === null) {
          var err = new Error('Not authorized! Go back!');
          err.status = 400;
          return next(err);
        }else if(req.body.bankName === "flashQuestions") {
            var err = new Error('Don\'t delete that!');
            err.status = 400;
            return next(err);
        } else if(req.body.bankName === "") {
            var err = new Error('Field blank');
            err.status = 400;
            return next(err);
        } else {
              db.collection(req.body.bankName).drop(function(err){
            if(err)
              throw err;
          });
          if(req.session.userId !== null)
                {
                         return res.render('profile', { user : user.username, email : user.email, access : user.accessLevel});
                }
                else
                {
                         return res.send({ user : user.username, email : user.email, access : user.accessLevel});
                 }

        }
      }
    });
});



app.get('/listBanks', function(req, res, next){
  console.log("listBanks called..");
  var bankNames = db.db.listCollections().toArray(function(err, names){
    console.log(names);
    return res.send(names);
  });
});

app.post('/addToOtherBank', function (req, res, next) {
  user = db.collection('users').findOne({ username: req.body.username }).then(function (user) {
    console.log('addToOtherBank called..');
    if (user === null) {
      var err = new Error('Not authorized! Go back!');
      err.status = 400;
      return next(err);
    } else if (req.body.question === "") {
      var err = new Error('Field blank');
      err.status = 400;
      return next(err + "blank");
    } else {
      db.db.listCollections({ name: req.body.targetCollection }).next(function (err, collinfo) {
        if (collinfo) {
          question = db.collection(req.body.sourceCollection).findOne({ question: req.body.question }).then(function (addQuestion) {
            if (addQuestion) {
              db.collection(req.body.targetCollection).insert(addQuestion);
              console.log(addQuestion);
              res.send(addQuestion.question);
            } else {
              console.log('Question does not exist!');
              var err = new Error('Question does not exist!');
              return next(err);
            }
          });
        } else {
          console.log('Collection does not exist!')
          var err = new Error('Collection does not exist!');
          return next(err);
        }
      });
    }
  });
});

app.post('/getFromBank', (req, res) => {
    db.collection(req.body.bankName).find({}).toArray(function(err, data) {
        if (err) console.log(err);
        else{
        console.log(data);
        res.send(data);
        }
    });
});

/**
 * .listen on port:48821 with launch alter to console
 */
app.listen(jsonRoute.port, ()=>console.log("NULL SERVERED LAUNCHED. LISTENING ON PORT: " + jsonRoute.port));


/**                                                                                                                                                                                                         
 * Verify function                                                                                                                                                                                          
 */
app.post('/verify', function(req, res){
    user = db.collection('users').findOne({username : req.body.username }).then(function(user){
        if (user === null) {
            var err = new Error('Not authorized!');
            err.status = 400;
            return next(err);
        } else {
            return res.send({access : user.accessLevel});
            };
    })
})

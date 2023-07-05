package Server;

import Server.Controllers.UserActionsController;
import Server.Controllers.DataController;
import Server.Database.DatabaseController;
import Server.Database.MessageData.Tweets;
import Server.Database.UserData.BlockList;
import Server.Database.UserData.Followers;
import Server.Database.UserData.Users;
import Server.Utils.Http.ServerHttpUtils;
import com.twitter.common.Annotations.APIEndpoint;
import com.twitter.common.Models.Messages.Visuals.Image;
import com.twitter.common.Models.Timeline;
import com.twitter.common.Models.UserGraph;
import com.twitter.common.Utils.JwtUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.twitter.common.API.API;
import com.twitter.common.Models.Messages.Textuals.Mention;
import com.twitter.common.Models.Messages.Textuals.Quote;
import com.twitter.common.Models.Messages.Textuals.Retweet;
import com.twitter.common.Models.Messages.Textuals.Tweet;
import com.twitter.common.Models.User;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;

import static Server.Utils.Http.ServerHttpUtils.*;
import static com.twitter.common.API.StatusCode.*;
import static com.twitter.common.Utils.SafeCall.safe;

@SuppressWarnings("unused") // the methods are used but since they're passed indirectly using reflection the ide can not recognize that
public class ServerRequestHandler {
    private HttpServer httpServer;
    private static final DatabaseController databaseController = DatabaseController.getInstance();
    private static final UserActionsController requestActionHandler = UserActionsController.getInstance();
    private static final DataController dataController = DataController.getInstance();
    public void run() {
        //TODO: try to move db initializations out of here!
        safe(()->{
            //zero indicates that http server will use default amount for backlog (how many users to handle concurrently)
            httpServer = HttpServer.create(new InetSocketAddress(API.SERVER_IP,API.PORT), 0);
            httpServer.setExecutor(Executors.newFixedThreadPool(8));
            databaseController.initializeDB();
            System.out.println("database initialized...");
            createContexts();
            System.out.println("api endpoints connected to the server");
            httpServer.start();
            System.out.println("server started working...");
        });
    }

    private void createContexts() {
        Method[] methods = ServerRequestHandler.class.getDeclaredMethods();

        for (Method method: methods) {
            if(method.isAnnotationPresent(APIEndpoint.class)) {
                APIEndpoint annotation = method.getAnnotation(APIEndpoint.class);
                httpServer.createContext(
                    annotation.endpoint(),
                    exchange ->
                    {
                        try {
                            method.invoke(this, exchange);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                );
            }
        }
    }

    private static boolean JwtCheck(HttpExchange exchange, int userId) {
        HashMap<String, Object> validClaims = new HashMap<>(2);
        Headers header = exchange.getRequestHeaders();
        if (header.get(JwtUtils.name).size() > 1 || !JwtUtils.JwtValidator(header.getFirst(JwtUtils.name), validClaims)) {
            sendErrorResponse(exchange, "you are logged out, please login again", UNAUTHORIZED);
            return false;
        }
        return true;
    }

    @APIEndpoint(endpoint = API.SIGN_UP)
    private static void signUp(HttpExchange exchange) {
        //required http method for sign up is POST
        if(ServerHttpUtils.validateMethod("POST", exchange)) {
            User toSignUp = validateBody(exchange, User.class);
            if(toSignUp == null) {
                badRequest(exchange);
            }
            else if (databaseController.emailExists(toSignUp.getEmail())) {
                sendErrorResponse(exchange, "email already in use.", DUPLICATE_RECORD);

            } else if (databaseController.usernameExists(toSignUp.getUsername())) {
                sendErrorResponse(exchange, "username already in use.", DUPLICATE_RECORD);

            } else if (toSignUp.getDateOfBirth().after(User.getLegalAge())) {
                sendErrorResponse(exchange, "sorry you are not 18 yet.", NOT_ALLOWED);

            } else if (requestActionHandler.signUp(toSignUp)) {
                sendSuccessResponse(exchange, "Sign up successfully done.", null);
            }
        }
    }

    @APIEndpoint(endpoint = API.SIGN_IN)
    private static void signIn(HttpExchange exchange) {
        //required http method for sign-in is GET  since after validation
        // of credentials all user data will be sent back to the client
        if(validateMethod("GET", exchange)) {
            Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
            if(validateEssentialKeys(exchange, query, Users.COL_USERNAME, Users.COL_PASSWORD_HASH)) {
                User user = requestActionHandler.signIn(query.get(Users.COL_USERNAME), query.get(Users.COL_PASSWORD_HASH));
                if (user == null) {
                    sendErrorResponse(exchange, "username or password incorrect", UNAUTHORIZED);
                } else {
                    //sends Jwt back to the client inside the user object
                    user.setJwt(JwtUtils.refreshTokenGenerator(user.getUserId()));
                    serializableResponse(
                            exchange,
                            "sign in successful",
                            user,
                            SUCCESS,
                            true);
                }
            }
        }
    }
    @APIEndpoint(endpoint = API.FOLLOW)
    @SuppressWarnings("unchecked")
    private static void follow(HttpExchange exchange) {
        if(validateMethod("POST", exchange)) {
            Map<String, String> body = ServerHttpUtils.validateBody(exchange, HashMap.class);

            if(validateEssentialKeys(exchange, body, Followers.COL_FOLLOWED, Followers.COL_FOLLOWER)) {
                int followerId = Integer.parseInt(body.get(Followers.COL_FOLLOWER));
                int followedId = Integer.parseInt(body.get(Followers.COL_FOLLOWED));

                if (JwtCheck(exchange, followerId)) {
                    if (followedId == followerId) {
                        sendErrorResponse(exchange, "following user failed", NOT_ALLOWED);

                    } else if (requestActionHandler.follow(followerId, followedId)) {
                        sendSuccessResponse(exchange, "user followed successfully", null);

                    } else {
                        sendErrorResponse(exchange, "you are already following this user", DUPLICATE_RECORD);
                    }
                }
            }
        }
    }
    @APIEndpoint(endpoint = API.UNFOLLOW)
    @SuppressWarnings("unchecked")
    private static void unfollow(HttpExchange exchange) {
        if(validateMethod("POST", exchange)) {
            Map<String, String> query = ServerHttpUtils.validateBody(exchange, HashMap.class);

            if(validateEssentialKeys(exchange, query, Followers.COL_FOLLOWED, Followers.COL_FOLLOWER)) {
                int followerId = Integer.parseInt(query.get(Followers.COL_FOLLOWER));
                int followedId = Integer.parseInt(query.get(Followers.COL_FOLLOWED));

                if (JwtCheck(exchange, followerId)) {
                    if (followedId == followerId) {
                        sendErrorResponse(exchange, "you can't follow or unfollow yourself", NOT_ALLOWED);

                    } else if (requestActionHandler.unfollow(followerId, followedId)) {
                        sendSuccessResponse(exchange, "user unfollowed successfully", null);

                    } else {
                        sendErrorResponse(exchange, "you do not follow this user", NOT_FOUND);
                    }
                }
            }
        }
    }

    @APIEndpoint(endpoint = API.TWEET)
    private static void tweet(HttpExchange exchange) {
        if(ServerHttpUtils.validateMethod("POST", exchange)) {
            Tweet toBeTweeted = validateSerializedBody(exchange, Tweet.class);
            if(toBeTweeted == null || toBeTweeted.getSender() == null) {
                badRequest(exchange);
            }
            if (JwtCheck(exchange, toBeTweeted.getSender().getUserId())) {
                if (requestActionHandler.tweet(toBeTweeted)) {
                    sendSuccessResponse(
                            exchange,
                    "Tweet successfully sent.",
                            null);
                }
                else {
                    internalServerError(exchange);
                }
            }
        }
    }

    @APIEndpoint(endpoint = API.QUOTE)
    private static void quote(HttpExchange exchange) {
        if(validateMethod("POST", exchange)) {
            Quote quote = validateSerializedBody(exchange, Quote.class);
            if (JwtCheck(exchange, quote.getSender().getUserId())) {
                if(requestActionHandler.quote(quote)) {
                    sendSuccessResponse(
                            exchange,
                    "tweet successfully quoted",
                        null);
                } else {
                    internalServerError(exchange);
                }
            }
        }
    }

    @APIEndpoint(endpoint = API.RETWEET)
    private static void retweet(HttpExchange exchange) {
        if(ServerHttpUtils.validateMethod("POST", exchange))
        {
            Retweet toBeReTweeted = validateSerializedBody(exchange, Retweet.class);
            if(toBeReTweeted == null) {
                badRequest(exchange);
            }
            if (JwtCheck(exchange, toBeReTweeted.getSender().getUserId())) {
                if (requestActionHandler.retweet(toBeReTweeted)) {
                    sendSuccessResponse(
                            exchange,
                    "retweeted successfully",
                            null);
                } else {
                    internalServerError(exchange);
                }
            }
        }
    }
    @APIEndpoint(endpoint = API.MENTION)
    private static void mention(HttpExchange exchange) {
        if(validateMethod("POST", exchange)) {
            Mention mention = validateSerializedBody(exchange, Mention.class);
            if (JwtCheck(exchange, mention.getSender().getUserId())) {
                if(requestActionHandler.mention(mention)) {
                    sendSuccessResponse(
                            exchange,
                            "tweet mentioned successfully",
                            null);
                } else {
                    internalServerError(exchange);
                }
            }
        }
    }

    @APIEndpoint(endpoint = API.LIKE)
    @SuppressWarnings("unchecked")
    private static void like(HttpExchange exchange) {
        if(validateMethod("POST", exchange)) {
            Map<String, String> query = validateBody(exchange, Map.class);

            if(validateEssentialKeys(exchange, query, Users.COL_USERID, Tweets.COL_TWEET_ID)) {
                int likerId = Integer.parseInt(query.get(Users.COL_USERID));
                long tweetId = Long.parseLong(query.get(Tweets.COL_TWEET_ID));

                if (JwtCheck(exchange, likerId)) {
                    if (requestActionHandler.like(likerId, tweetId)) {
                        sendSuccessResponse(exchange, "liked successfully", null);
                    } else {
                        internalServerError(exchange);
                    }
                }
            }
        }
    }

    @APIEndpoint(endpoint = API.UNLIKE)
    @SuppressWarnings("unchecked")
    private static void unlike(HttpExchange exchange) {
        if(validateMethod("POST", exchange)) {
            Map<String, String> query = validateBody(exchange, Map.class);

            if(validateEssentialKeys(exchange, query, Users.COL_USERID, Tweets.COL_TWEET_ID)) {
                int unLikerId = Integer.parseInt(query.get(Users.COL_USERID));
                long tweetId = Long.parseLong(query.get(Tweets.COL_TWEET_ID));

                if (JwtCheck(exchange, unLikerId)) {
                    if (requestActionHandler.unlike(unLikerId, tweetId)) {
                        sendSuccessResponse(exchange, "unliked successfully", null);
                    } else {
                        internalServerError(exchange);
                    }
                }
            }
        }
    }

    @APIEndpoint(endpoint = API.BLOCK)
    @SuppressWarnings("unchecked")
    private static void block(HttpExchange exchange) {
        if(validateMethod("POST", exchange)) {
            Map<String, String> params = validateBody(exchange, HashMap.class);

            if(validateEssentialKeys(exchange, params, BlockList.COL_BLOCKED, BlockList.COL_BLOCKED)) {
                int blockerId = Integer.parseInt(params.get(BlockList.COL_BLOCKER));
                int blockedId = Integer.parseInt(params.get(BlockList.COL_BLOCKED));

                if (JwtCheck(exchange, blockerId)) {
                    if (blockedId == blockerId) {
                        sendErrorResponse(exchange, "you can't block/unblock yourself", NOT_ALLOWED);

                    } else if (requestActionHandler.block(blockerId, blockedId)) {
                        sendSuccessResponse(exchange, "user successfully blocked", null);

                    } else {
                        sendErrorResponse(exchange, "you have already blocked this user", DUPLICATE_RECORD);
                    }

                }
            }
        }
    }

    @APIEndpoint(endpoint = API.UNBLOCK)
    @SuppressWarnings("unchecked")
    private static void unblock(HttpExchange exchange) {
        if(validateMethod("POST", exchange)) {
            Map<String, String> query = validateBody(exchange, HashMap.class);

            if(validateEssentialKeys(exchange, query, BlockList.COL_BLOCKED, BlockList.COL_BLOCKED)) {
                int blockerId = Integer.parseInt(query.get(BlockList.COL_BLOCKER));
                int blockedId = Integer.parseInt(query.get(BlockList.COL_BLOCKED));

                if (JwtCheck(exchange, blockerId)) {
                    if (blockedId == blockerId) {
                        sendErrorResponse(exchange, "you can't block/unblock yourself", NOT_ALLOWED);

                    } else if (requestActionHandler.unblock(blockerId, blockedId)) {
                        sendSuccessResponse(exchange, "user successfully unblocked", null);

                    } else {
                        sendErrorResponse(exchange, "you have not blocked this person", NOT_FOUND);
                    }

                }
            }
        }
    }

    @APIEndpoint(endpoint = API.SET_PROFILE_PIC)
    private static void setProfilePicture(HttpExchange exchange) {
        setUserVisualAttribute(exchange, "Profile", requestActionHandler::setProfile);
    }

    @APIEndpoint(endpoint = API.SET_HEADER)
    private static void setHeader(HttpExchange exchange) {
        setUserVisualAttribute(exchange, "Header", requestActionHandler::setHeader);
    }


    @APIEndpoint(endpoint = API.SET_DISPLAY_NAME)
    private static void setDisplayName(HttpExchange exchange) {
        setUserTextualAttribute(exchange, Users.COL_DISPLAY_NAME, requestActionHandler::setDisplayName);
    }

    @APIEndpoint(endpoint = API.SET_BIO)
    private static void setBio(HttpExchange exchange) {
        setUserTextualAttribute(exchange, Users.COL_BIO, requestActionHandler::setBio);
    }

    @APIEndpoint(endpoint = API.SET_LOCATION)
    private static void setLocation(HttpExchange exchange) {
        setUserTextualAttribute(exchange, Users.COL_LOCATION, requestActionHandler::setLocation);
    }

    @APIEndpoint(endpoint = API.SET_USERNAME)
    private static void setUsername(HttpExchange exchange) {
        //TODO: to be implemented
    }

    @APIEndpoint(endpoint = API.GET_TIMELINE)
    private static void getTimeLine(HttpExchange exchange) {
        if(validateMethod("GET", exchange)) {
            Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
            int userId = Integer.parseInt(query.get(Users.COL_USERID));
            int max = Integer.parseInt(query.get("max")); //TODO: don't use hardcoded text here
            if(max == 0 || userId == 0) {
                badRequest(exchange);
            }
            if(JwtCheck(exchange, userId)) {
                Timeline timeline = dataController.getTimeline(userId, max);
                serializableResponse(
                        exchange,
                "get timeline success",
                        timeline,
                        SUCCESS,
                true);
            }
        }
    }

    @APIEndpoint(endpoint = API.CHECK_EMAIL)
    private static void emailExists(HttpExchange exchange) {
        duplicateCheck(exchange, Users.COL_EMAIL);
    }

    @APIEndpoint(endpoint = API.CHECK_USERNAME)
    private static void usernameExists(HttpExchange exchange) {
        duplicateCheck(exchange, Users.COL_USERNAME);
    }

    private static void duplicateCheck(HttpExchange exchange, String type) {
        if(validateMethod("GET", exchange)) {
            Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
            String username = query.get(type);
            if(validateEssentialKeys(exchange, query, type)) {
                if (type.equals(Users.COL_USERNAME)) {
                    sendSuccessResponse(
                            exchange,
                            "duplicate username",
                            databaseController.usernameExists(username));

                } else if (type.equals(Users.COL_EMAIL)) {
                    sendSuccessResponse(
                            exchange,
                            "duplicate email",
                            databaseController.emailExists(username));
                }
            }
        }
    }

    @APIEndpoint(endpoint = API.GET_PROFILE)
    private static void getUserProfile(HttpExchange exchange) {
        if(validateMethod("GET", exchange)) {
            Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
            if(validateEssentialKeys(exchange, query, Users.COL_USERID)) {
                int userId = Integer.parseInt(query.get(Users.COL_USERID));
                User user = dataController.getUser(userId);

                if(user != null) {
                    serializableResponse(
                            exchange,
                    "user found successfully",
                            user,
                            SUCCESS,
                    true);
                }
            }
        }
    }

    @APIEndpoint(endpoint = API.GET_FOLLOWERS_COUNT)
    private static void getFollowersCount(HttpExchange exchange) {
        int count = getCountHelper(exchange, "follower");
        if(count >= 0) sendSuccessResponse(exchange, "followers count retrieved successfully", count);
    }


    @APIEndpoint(endpoint = API.GET_FOLLOWERS)
    private static void getFollowers(HttpExchange exchange) {
        userListHelper(exchange, "followers", dataController::getFollowers);
    }


    @APIEndpoint(endpoint = API.GET_FOLLOWINGS_COUNT)
    private static void getFollowingsCount(HttpExchange exchange) {
        int count = getCountHelper(exchange, "following");
        if(count >= 0) sendSuccessResponse(exchange, "followers count retrieved successfully", count);
    }


    @APIEndpoint(endpoint = API.GET_FOLLOWINGS)
    private static void getFollowings(HttpExchange exchange) {
        userListHelper(exchange, "followings", dataController::getFollowings);
    }

    @APIEndpoint(endpoint = API.SEARCH_USERS)
    private static void searchForUser(HttpExchange exchange) {
        if(validateMethod("GET", exchange)) {
            final String SEARCH_TERM_PARAM_NAME = "search_term";
            Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());

            if(validateEssentialKeys(exchange, query, SEARCH_TERM_PARAM_NAME)) {
                String searchTerm = query.get(SEARCH_TERM_PARAM_NAME);
                List<User> searchResult = dataController.searchForUser(searchTerm);
                if(searchResult != null)
                    serializableResponse(
                            exchange,
                            "search done with " + searchResult.size() + "results.",
                            new UserGraph(searchResult),
                            SUCCESS,
                            true);
                else
                    internalServerError(exchange);
            }
        }
    }

    private static int getCountHelper(HttpExchange exchange, String type) {
        if(validateMethod("GET", exchange)) {
            Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
            if(validateEssentialKeys(exchange, query, Users.COL_USERID)) {
                int userId = Integer.parseInt(query.get(Users.COL_USERID));

                return switch (type) {
                    case "following" -> dataController.getFollowingsCount(userId);
                    case "follower" -> dataController.getFollowersCount(userId);
                    default -> -1;
                };
            }
        }
        return -2;
    }


    private static void userListHelper(HttpExchange exchange, String listName, Function<Integer, List<User>> getListFunction) {
        if (validateMethod("GET", exchange)) {
            Map<String, String> query = queryToMap(exchange.getRequestURI().getQuery());
            if (validateEssentialKeys(exchange, query, Users.COL_USERID)) {
                int userId = Integer.parseInt(query.get(Users.COL_USERID));
                if (JwtCheck(exchange, userId)) {
                    List<User> userList = getListFunction.apply(userId);
                    if (userList != null) {
                        serializableResponse(
                                exchange,
                                listName + " list retrieved successfully",
                                new UserGraph(userList),
                                SUCCESS,
                                true);
                    } else {
                        internalServerError(exchange);
                    }
                }
            }
        }
    }

    private static void setUserVisualAttribute(HttpExchange exchange, String imageType, BiFunction<Integer, Image, Boolean> setter) {
        if(validateMethod("POST", exchange)) {
            Image newImage = validateSerializedBody(exchange, Image.class);
            int userId = Integer.parseInt(getValueFromHeader(exchange, Users.COL_USERID));
            if (newImage == null || userId == 0) {
                badRequest(exchange);
            } else if (JwtCheck(exchange, userId)) {
                if (setter.apply(userId, newImage)) {
                    sendSuccessResponse(
                            exchange,
                            imageType + " changed successfully",
                            null);
                } else {
                    internalServerError(exchange);
                }
            }
        }
    }


    private static void setUserTextualAttribute(HttpExchange exchange, String newAttributeName, BiFunction<Integer, String, Boolean> setter) {
        if(validateMethod("POST", exchange)) {
            int userId = Integer.parseInt(getValueFromHeader(exchange, Users.COL_USERID));
            String newAttribValue = getValueFromHeader(exchange, newAttributeName);
            if(userId == 0 || newAttribValue == null) {
                badRequest(exchange);
            }
            if(setter.apply(userId, newAttribValue)) {
                sendSuccessResponse(
                        exchange,
                        newAttributeName + " changed successfully!",
                        null);
            } else {
                internalServerError(exchange);
            }
        }
    }
}

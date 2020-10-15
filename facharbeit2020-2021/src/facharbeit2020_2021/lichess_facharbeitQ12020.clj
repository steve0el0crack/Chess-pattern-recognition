(ns escaob.lichess.core
  (:require [org.httpkit.client :as http]  ;; "Like the Server, the client uses an event-driven, non-blocking I/O model." (http://http-kit.github.io/)
            [clojure.data.json :as json])
  (:import [org.httpkit.client ClientSslEngineFactory]))

;; The Lichess API describes more target points than just "/account", for example there is also "/preferences" (https://lichess.org/api#operation/accountEmail). So, I am defining a basis function called query, to which when passed nil as argument; returns "get" for "/account".

(defn query-get
  [ext]
  (let [target (str "https://lichess.org/api" ext)]
    @(http/get target
               {:headers {"Authorization" "Bearer PAkhhpIdIdKQeBvi"}
                :sslengine 
                (ClientSslEngineFactory/trustAnybody)})))

;; Lichess-data brings a lot of information from my Lichess profile or account (https://lichess.org/@/EstebanRicardo).
;; This is contained within the body of what I receive as response. All the other keys and stuff belongs to httpkit.client properties, for example the "keep-alive" attribute.

=> {:opts {:headers {"Authorization" "Bearer PAkhhpIdIdKQeBvi"},
           :sslengine #object[sun.security.ssl.SSLEngineImpl 0x48476b06 "sun.security.ssl.SSLEngineImpl@48476b06"],
           :method :get,  ;;it would be interesting to see what other methods there are... https://github.com/http-kit/http-kit
           :url "https://lichess.org/api/account"},
    :body "{\"id\":\"estebanricardo\",\"username\":\"EstebanRicardo\",\"online\":true,\"perfs\":{\"blitz\":{\"games\":98,\"rating\":1637,\"rd\":99,\"prog\":2},\"puzzle\":{\"games\":111,\"rating\":1951,\"rd\":161,\"prog\":26,\"prov\":true},\"bullet\":{\"games\":20,\"rating\":1348,\"rd\":106,\"prog\":106},\"correspondence\":{\"games\":10,\"rating\":1361,\"rd\":143,\"prog\":0,\"prov\":true},\"classical\":{\"games\":7,\"rating\":1763,\"rd\":221,\"prog\":0,\"prov\":true},\"rapid\":{\"games\":98,\"rating\":1738,\"rd\":82,\"prog\":-34}},\"createdAt\":1474821213438,\"profile\":{\"country\":\"DE\",\"location\":\"Bonn\",\"bio\":\"A hacker with Clojure, Python, and Java. Of course functional programmer!  GitHub Account\\r\\nFisher and Kasparov are my favorite players, and I would really enjoy analyzing some of their games with other people.\\r\\nHandball player at HSG Siebengebirge.\",\"firstName\":\"Esteban\",\"lastName\":\"Esteban Ricardo\",\"fideRating\":1462,\"links\":\"https://www.chess.com/member/esteban_ricardo\\r\\nhttps://github.com/steve0el0crack\"},\"seenAt\":1591868456735,\"playTime\":{\"total\":145461,\"tv\":0},\"language\":\"en-GB\",\"url\":\"https://lichess.org/@/EstebanRicardo\",\"nbFollowing\":1,\"nbFollowers\":5,\"completionRate\":20,\"count\":{\"all\":269,\"rated\":233,\"ai\":0,\"draw\":6,\"drawH\":6,\"loss\":124,\"lossH\":124,\"win\":139,\"winH\":139,\"bookmark\":1,\"playing\":0,\"import\":0,\"me\":0},\"followable\":true,\"following\":false,\"blocking\":false,\"followsYou\":false}",
    :headers {:date "Thu, 11 Jun 2020 09:41:59 GMT",
              :server "nginx",
              :vary "Origin",
              :expect-ct "max-age=31536000, enforce, report-uri=\"https://monitor.lichess.ovh/report/ct\"",
              :report-to "{\"group\":\"default\",\"max_age\":31536000,\"endpoints\":[{\"url\":\"https://monitor.lichess.ovh/report/default\"}],\"include_subdomains\":true}",
              :x-frame-options "DENY",
              :strict-transport-security "max-age=31536000; includeSubDomains; preload",
              :content-type "application/json",  ;;don't know what json is or how it works...
              :access-control-allow-methods "OPTIONS, GET, POST",  ;;this looks important. 
              :content-encoding "gzip",
              :access-control-allow-origin "*",  ;;this too.
              :connection "keep-alive",
              :x-oauth-scopes "preference:read, email:read, challenge:read, puzzle:read", ;;I've set these option at the moment of creating the token at https://lichess.org/account/oauth/token
              :nel "{\"report_to\":\"default\",\"max_age\":31536000,\"include_subdomains\":true,\"failure_fraction\":0.001}",
              :x-accepted-oauth-scopes "",
              :transfer-encoding "chunked",
              :access-control-allow-headers "Origin, Authorization, If-Modified-Since, Cache-Control"},
    :status 200}

;; As a resume of httpkit.client (http://http-kit.github.io/client.html) I can say that it is a try of integrating the native concepts of asynchronous handling technology of Clojure, into a server & client framework. This enables things like this: http://http-kit.github.io/blog.html. It is actually written in Clojure and Java.
;; It would be very helpful to see how http/get method actually works... (https://github.com/http-kit/http-kit/blob/master/src/org/httpkit/client.clj)

;; Anyway, what matters the most is the :body part of the response. My programm must be able to read & interpret it and then constructs useful representations of this data on some browser of mobile app. I am gonna explore these tools... 

;; The returned value is in json format, and therefore I must depure it in order to get something readable.

(defn depure-query
  [json]
  (->
   json
   :body
   (json/read-str :key-fn keyword)))

=> {:completionRate 20,
    :playTime {:total 145461, :tv 0},
    :nbFollowing 1,
    :username "EstebanRicardo",
    :nbFollowers 5,
    :createdAt 1474821213438,
    :following false,
    :online true,
    :perfs {:blitz {:games 98, :rating 1637, :rd 99, :prog 2},
            :puzzle {:games 111, :rating 1951, :rd 161, :prog 26, :prov true},
            :bullet {:games 20, :rating 1348, :rd 106, :prog 106},
            :correspondence {:games 10, :rating 1361, :rd 143, :prog 0, :prov true},
            :classical {:games 7, :rating 1763, :rd 221, :prog 0, :prov true},
            :rapid {:games 98, :rating 1738, :rd 82, :prog -34}},
    :followable true,
    :language "en-GB",
    :id "estebanricardo",
    :count {:bookmark 1, :winH 139, :ai 0, :lossH 124, :all 269, :rated 233, :drawH 6, :playing 0, :draw 6, :loss 124, :import 0, :me 0, :win 139},
    :url "https://lichess.org/@/EstebanRicardo",
    :followsYou false,
    :blocking false,
    :seenAt 1591872353001,
    :profile {:country "DE", :location "Bonn", :bio "A hacker with Clojure, Python, and Java. Of course functional programmer!  GitHub Account\r\nFisher and Kasparov are my favorite players, and I would really enjoy analyzing some of their games with other people.\r\nHandball player at HSG Siebengebirge.",
              :firstName "Esteban",
              :lastName "Esteban Ricardo",
              :fideRating 1462,
              :links "https://www.chess.com/member/esteban_ricardo\r\nhttps://github.com/steve0el0crack"}}

;; And here will be calculated the number of total games I've ever played on Lichess.

(let [data (depure-query (query-get "/account"))]
    (->>
     (map
      (fn [game-type]
        (get-in data [:perfs game-type :games]))
      (keys (get-in data [:perfs])))
     (reduce + 0 )))

;; In the API it is also described how to access to public information of other players.

(depure-query "/user/yanchuz")

=> {:completionRate 100,
    :playTime {:total 846671, :tv 0},
    :nbFollowing 3,
    :username "yanchuz",
    :nbFollowers 1,
    :createdAt 1566284293810,
    :online false,
    :perfs {:chess960 {:games 4, :rating 1355, :rd 183, :prog 0, :prov true},
            :puzzle {:games 328, :rating 1578, :rd 66, :prog 26},
            :blitz {:games 1132, :rating 1437, :rd 45, :prog 6},
            :crazyhouse {:games 5, :rating 1323, :rd 170, :prog 0, :prov true},
            :bullet {:games 1379, :rating 1469, :rd 45, :prog -4},
            :correspondence {:games 10, :rating 1310, :rd 144, :prog 0, :prov true},
            :classical {:games 50, :rating 1442, :rd 80, :prog 98},
            :rapid {:games 76, :rating 1459, :rd 61, :prog 38}},
    :language "de-DE",
    :id "yanchuz",
    :count {:bookmark 11, :winH 1300, :ai 42, :lossH 1277, :all 2719, :rated 2657, :drawH 100, :playing 0, :draw 113, :loss 1293, :import 0, :me 0, :win 1313},
    :url "https://lichess.org/@/yanchuz",
    :seenAt 1591877858226,
    :profile {:country "CN",
              :location "Bonn",
              :firstName "Yanchu",
              :lastName "Zhang"}}

;; Sadly I still do not manage MACRO's that good enough, for not repeating this function... but soon this issue will be fixed!
;; The following function asks for the data of a list of players who must not be interrelated in any form. We just need a collection of ID's

(defn query-players
  [coll]
  (let [players (clojure.string/join "," coll)]
    @(http/post "https://lichess.org/api/users"
                {:headers {"Authorization" "Bearer PAkhhpIdIdKQeBvi"}
                :sslengine 
                (ClientSslEngineFactory/trustAnybody)
                 :body players})))  ;;the especification and usage of this method states that the body must be in "plain text" format.

;; So, I define my team with 2 players and then ask for their data. At the end Ihave two big hash-maps containing the info.
;; Actually it returns as answer, an array containing a hashmap for each of the player asked for.

(def players-coll (depure-query (query-players ["estebanricardo" "yanchuz"])))

;; The next is to ask for a concrete TEAM, that is an agrupation of players with some afinity.
;; We only need the ID of the team.

(defn get-team
  [ID]
  (let [raw-query @(http/get (str "https://lichess.org/api/team/" ID "/users")
                             {:headers {"Authorization" "Bearer PAkhhpIdIdKQeBvi"}
                              :sslengine 
                              (ClientSslEngineFactory/trustAnybody)})
        str-coll (-> raw-query  ;; We receive a BIG STRING and we must work on it!
                     :body      ;; The detailed description would be: "{}\n{}\n{}\n...", where each one of "{...}" is a PERSON structure.
                     clojure.string/split-lines  ;; With this fn we can finally obtain each player, all contained in a BIG ARRAY.
                     )]
    (map (fn [str] (json/read-str str :key-fn keyword)) str-coll)))

;; The ID of the club does not contain UPPERCASE's and blankspace is represented via "-" 
;; Above a brief example of the use.n
(count  (get-team "chess-crashcourse-2020"))  ;; https://lichess.org/team/cm-foxes-chess

;; The next step is to be able to "stream" a complete match. My programm will act just like a simulation. 
;; The match being played is attached to the profile of the user playing it. So, first of all I need access to this info.
;; I just need the token.

(def tokens {"estebanricardo" "PAkhhpIdIdKQeBvi"
             "rogo17" "xxx"
             "Schwabenpfeil" "xxx"})

(defn ongoinggame-of  ;;the most important ist that this function returns a key "lastMove" : c5c4 for example.
  [token]
  (-> @(http/get "https://lichess.org/api/account/playing"
                 {:headers {"Authorization" (str "Bearer " token)}
                  :sslengine 
                  (ClientSslEngineFactory/trustAnybody)})
      depure-query
      :nowPlaying))

(ongoinggame-of (tokens "esteban"))

(defn game-of  ;;this would be better to use if the match is already completed.
  [username]
  (-> @(http/get (str "https://lichess.org/api/user/" username "/current-game?evals=true")
                 {:headers {"Authorization" "Bearer PAkhhpIdIdKQeBvi"
                            "content-type" "application/json"}  ;;the API says we can get a json structure as response, but does not explain too well how to do the distinction between this json structure and the deffault chess-pgn.
                  :sslengine 
                  (ClientSslEngineFactory/trustAnybody)})))

;; The body of the response we receive is one of the type "org.httpkit.BytesInputStream"
;; We consume this stream and obtain a big ARRAY as a result... the complet PGN of the match!

(def pgn (slurp (:body (game-of "estebanricardo"))))




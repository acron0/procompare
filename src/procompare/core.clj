(ns procompare.core
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [environ.core :refer [env]])
  (:gen-class))

;; https://na1.api.riotgames.com/lol/summoner/v3/summoners/by-name/RiotSchmick?api_key=<key>
;; http://matchhistory.na.leagueoflegends.com/en/#match-details/TRLH1/1002360527?gameHash=8dfaab886a2ec130&tab=overview

#_(def config
    {:scrape/urls ["http://lol.esportswikis.com/wiki/Special:RunQuery/MatchHistoryTournament?MHT%5Btournament%5D=Concept:World%20Championship%202017%20Play-In&MHT%5Btext%5D=Yes&wpRunQuery=true"
                   "http://lol.esportswikis.com/wiki/Special:RunQuery/MatchHistoryTournament?MHT%5Btournament%5D=Concept:World%20Championship%202017%20Main%20Event&MHT%5Btext%5D=Yes&wpRunQuery=true"]})

(def region-endpoints
  {:br	 "br1.api.riotgames.com"
   :eune "eun1.api.riotgames.com"
   :euw	 "euw1.api.riotgames.com"
   :jp	 "jp1.api.riotgames.com"
   :kr	 "kr.api.riotgames.com"
   :lan	 "la1.api.riotgames.com"
   :las	 "la2.api.riotgames.com"
   :na	 "na1.api.riotgames.com"
   :oce	 "oc1.api.riotgames.com"
   :tr	 "tr1.api.riotgames.com"
   :ru	 "ru.api.riotgames.com"
   :pbe	 "pbe1.api.riotgames.com"})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn build-query
  [api region]
  (let [url (str "https://" (get region-endpoints region) "/" api)]
    url))

(defn send-query
  ([url]
   (send-query url {}))
  ([url query-params]
   (-> @(http/get url {:as :json
                       :content-type :json
                       :throw-exceptions false
                       :accept :json
                       :query-params (merge {"api_key" (env :riot-api-key)}
                                            query-params)})
       :body)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn summoners-summoner-by-name
  [summoner-name]
  (str "lol/summoner/v3/summoners/by-name/" summoner-name))

(defn summoners-summoner-by-name
  [summoner-name]
  (str "lol/summoner/v3/summoners/by-name/" summoner-name))

(defn matches-recent-matches-by-account
  [account-id]
  (str "/lol/match/v3/matchlists/by-account/" account-id "/recent"))

(defn matches-matches-by-account
  [account-id]
  (str "/lol/match/v3/matchlists/by-account/" account-id))

(defn matches-match-timeline-by-match-id
  [match-id]
  (str "/lol/match/v3/timelines/by-match/" match-id))

(defn matches-match-by-match-id
  [match-id]
  (str "/lol/match/v3/matches/" match-id))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-all-matches
  ([region user]
   (get-all-matches region user {:batches-of 1}))

  ([region {:keys [accountId]} {:keys [batches-of] :or {batches-of 1} :as opts}]
   (get-all-matches nil accountId region 0 batches-of))

  ([last-result account-id region start-index batches-of]
   (let [result (-> (matches-matches-by-account account-id)
                    (build-query :euw)
                    (send-query {:beginIndex start-index
                                 :endIndex (+ start-index batches-of)})
                    :matches)]
     (lazy-seq (concat result (get-all-matches result account-id region (+ start-index batches-of) batches-of))))))

(defn match->match-info
  [region]
  (fn [{:keys [gameId]}]
    (-> (matches-match-by-match-id gameId)
        (build-query region)
        (send-query))))

(defn match->match-timeline
  [region]
  (fn [{:keys [gameId]}]
    (-> (matches-match-timeline-by-match-id gameId)
        (build-query region)
        (send-query))))

(defn get-gold-over-time
  [region participant-id]
  (fn [{:keys [frames]}]
    (map (comp :totalGold participant-id :participantFrames) frames)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

#_(def player (-> "acron0"
                  (summoners-summoner-by-name)
                  (build-query :euw)
                  (send-query)))

#_(def match (->> player
                  (get-all-matches :euw)
                  (map (match->match-timeline :euw))
                  (map (get-gold-over-time :euw :1))
                  (take 1)))

#_(map (comp :totalGold :2 :participantFrames) (:frames (first match)))



(take 1 (transduce (comp (map (match->match-timeline :euw))
                         (map (get-gold-over-time :euw :1)))
                   concat
                   (->> (summoners-summoner-by-name "acron0")
                        (get-all-matches :euw))))

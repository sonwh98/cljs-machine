(ns stigmergy.cdr.dir-navigator
  (:require [stigmergy.tily :as tily]
            [reagent.core :as r]
            [taoensso.timbre :as log :include-macros true]))

(defn toggle [node evt]
  (let [element (.-target evt)
        parent (.-parentElement element)]
    (swap! node update :visible? not)
    (.. parent (querySelector ".sub-dir") -classList (toggle "active"))))

(defn get-name [node]
  (-> node keys first))

(defn get-children [node]
  (-> node vals first))

(defn dir [{:keys [node on-click] :as args}]
  [:li
   [:span {:class "dir"
           :on-click #(toggle node %)} (get-name @node)]
   [:ul {:class (if (:visible? @node)
                  "sub-dir active"
                  "sub-dir")
         :style {:list-style-type :none}}
    (let [index-children (-> @node get-children tily/with-index)]
      (doall (for [[index c] index-children
                   :let [k (-> @node keys first)
                         child (r/cursor node [k index])]]
               (with-meta (if-let [file-name (:name c)]
                            [:li {:on-click #(on-click c)
                                  :on-context-menu #(do
                                                      (.. % preventDefault)
                                                      (js/alert "right"))} file-name]
                            [dir (merge args {:node child}) ])
                 {:key (str c)}))))]])

(defn tree [{:keys [node on-click] :as args}]
  (when-not (empty? @node)
    [:ul {:style {:list-style-type :none
                  :overflow :auto
                  :margin 0
                  :padding 0}}
     [dir args]]))

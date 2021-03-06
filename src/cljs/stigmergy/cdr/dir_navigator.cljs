(ns stigmergy.cdr.dir-navigator
  (:require [stigmergy.tily :as tily]
            [reagent.core :as r]
            [taoensso.timbre :as log :include-macros true]
            [stigmergy.cdr.state :as state]
            [stigmergy.cdr.node :as n]
            ))

(defn toggle [node evt]
  (let [element (.-target evt)
        parent (.-parentElement element)]
    (swap! node update :visible? not)
    (.. parent (querySelector ".sub-dir") -classList (toggle "active"))))

(defn context-menu-handler [evt on-context-menu]
  (let [x (- (.-clientX evt) 15)
        y (.-clientY evt)]
    (.. evt preventDefault)
    (on-context-menu x y)))

(defn select-node [node]
  (swap! state/app-state assoc :selected-node node))

;;TODO refactor to prevent excess re-render of parent when child changes
(defn dir [{:keys [node on-click on-context-menu] :as args}]
  ;;(prn "dir=" (keys @node))
  [:li
   [:span {:class "dir"
           :on-click #(do
                        (select-node @node)
                        (toggle node %))
           :on-context-menu  #(do
                                (select-node @node)
                                (context-menu-handler % on-context-menu))}
    (n/get-name @node)]
   
   [ :ul {:class (if (:visible? @node)
                   "sub-dir active"
                   "sub-dir")
          :style {:list-style-type :none}}
    (when (:visible? @node)
      (let [index-children (-> @node n/get-children tily/with-index)]
        (doall (for [[index c] index-children
                     :let [k (-> @node keys first)
                           child (r/cursor node [k index])]]
                 (with-meta (if (n/file? c)
                              [:li {:on-click #(do
                                                 (select-node c)
                                                 (on-click c))
                                    :on-context-menu #(do
                                                        (select-node c)
                                                        (context-menu-handler % on-context-menu))}
                               (:file/name c)]
                              [dir (merge args {:node child}) ])
                   {:key (str c)})))))]])


(defn tree [{:keys [node] :as args}]
  [:ul {:style {:list-style-type :none
                :overflow :auto
                :margin 0
                :padding 0}}
   [dir args]])


(comment
  (get-in @app-state [:projects "tweenie" :src-tree "tweenie"  1])
  (get-in @app-state [:projects "tweenie" :src-tree "tweenie" ])
  (get-in @app-state [:projects "lightning-fs" :src-tree "lightning-fs" ])


  )


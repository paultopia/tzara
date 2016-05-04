;; this namespace just contains a function to create a term-document matrix out of a collection of 
;; tokenized strings (seqable of seqable) (vectors, sequences, whev.).  

;; There are two public functions.  The first, td-maps, takes a straight seqable of seqable, 
;; and produces a vector of maps where each map represents a document and is 
;; {:token1 count token2: count...} for every token that appears in the entire dataset
;;
;; The second, td-matrix takes a td-map and converts it to a single map: 
;; {:labels [vector of token labels] :frequencies [vec of seqs]} where the 
;; frequency vector-of-seqs is in standard term-document-matrix format (i.e., 
;; documents on the rows, counts on the columns) and labels are keywords
;; 
;; so obviously, the thing to do ordinarily is (td-matrix (td-map your-tokenized-dataset))
;;
;; this should all just pass seamlessly to any core.matrix flavor.

(ns blabber.tdm)

;; private functions

(defn- count-strings
  "count frequencies of strings in vector of vectors of strings"
  [stringvecs]
  (mapv frequencies stringvecs))

(defn- list-strings
  "list all strings in doc set"
  [token-vecs]
  (distinct
    (apply concat token-vecs)))

(defn- cartesian-map
  [stringlist]
  (zipmap stringlist (repeat 0)))

(defn- sparsify-counts
  "based on strings in all preprocesed docs, fill counts with 0 for unused strings in each single preprocessed doc"
  [zeroes counts]
  (mapv #(merge-with + % zeroes) counts))


;; public functions follow

(defn td-maps
  "vector of tokenized documents --> vector of maps with counts, including zeroes for terms 
  that do not appear in a given document"
  [token-vecs]
    (sparsify-counts
      (-> token-vecs list-strings cartesian-map)
      (-> token-vecs count-strings)))

(defn td-matrix
  "vector of matrices --> TDM proper as map w/ :labels :data"
  [tdmaps]
  (let [smaps (mapv #(into (sorted-map) %) tdmaps)]
    {:labels (keys (first smaps)) 
     :frequencies (mapv vals smaps)}))

;; this is all tested and correct, albeit in clojurescript rather than clojure...

;; (def testdocs [["One" "Two" "Three"] ["Ay" "Bee" "Cee"] ["One" "Ay" "One" "Bee" "One" "Ay"]])
;; (td-maps testdocs)
;; [{"One" 1, "Two" 1, "Three" 1, "Ay" 0, "Bee" 0, "Cee" 0} {"Ay" 1, "Bee" 1, "Cee" 1, "One" 0, "Two" 0, "Three" 0} {"One" 3, "Ay" 2, "Bee" 1, "Two" 0, "Three" 0, "Cee" 0}]
;; (td-matrix (td-maps testdocs))
;; {:labels ("Ay" "Bee" "Cee" "One" "Three" "Two"), :frequencies [(0 0 0 1 1 1) (1 1 1 0 0 0) (2 1 0 3 0 0)]}

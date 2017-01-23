require(Matrix)
read.libsvm = function( filename ) {
  content = readLines( filename )
  num_lines = length( content )
  tomakemat = cbind(1:num_lines, -1, substr(content,1,1))

  # loop over lines
  makemat = rbind(tomakemat,
  do.call(rbind,
    lapply(1:num_lines, function(i){
       # split by spaces, remove lines
           line = as.vector( strsplit( content[i], ' ' )[[1]])
           cbind(i, t(simplify2array(strsplit(line[-1],
                          ':'))))
})))
class(makemat) = "numeric"

#browser()
yx = sparseMatrix(i = makemat[,1],
              j = makemat[,2]+2,
          x = makemat[,3])
return( yx )
}

require(caret)
a <- read.libsvm("/Users/tom/projects-workspace/set-game/data/train-out-colour.svm")
b <- as.data.frame(as.matrix(a))

nzv <- nearZeroVar(b)
c <- b[, -nzv]

c$V1 <- as.factor(c$V1) # make colour a factor
set.seed(1)
cFit <- train(V1 ~ ., data = c, method = "svmRadial", preProc = c("center", "scale"), tuneLength = 10,
  tr = trainControl(method = "repeatedcv", repeats = 5, classProbs = TRUE))
cFit

cFitKnn <- train(V1 ~ ., data = c, method = "knn", preProc = c("center", "scale"))
cFitKnn

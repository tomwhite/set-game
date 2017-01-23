require(caret)
a <- read.csv("/Users/tom/projects-workspace/set-game/data/train-out-shading.csv")
a$X1 <- as.factor(a$X1)
set.seed(1)
aFit <- train(X1 ~ ., data = a, method = "svmRadial", preProc = c("center", "scale"), tuneLength = 10)
aFit

aFitKnn <- train(X1 ~ ., data = a, method = "knn", preProc = c("center", "scale"))
aFitKnn

aFitRf <- train(X1 ~ ., data = a, method = "rf")
aFitRf

  library(MASS)
a.lda <- lda(X1 ~ . , data = a)
table(predict(a.lda, type="class")$class, a$X1)

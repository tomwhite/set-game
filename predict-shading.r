require(caret)
a <- read.csv("/Users/tom/projects-workspace/set-game/data/train-out-shading.csv", col.names = c("label", "v1"))
a$label <- as.factor(a$label)
set.seed(1)
aFit <- train(label ~ ., data = a, method = "svmRadial", preProc = c("center", "scale"),
              tuneLength = 10,
              tr = trainControl(method = "repeatedcv", repeats = 5, classProbs = TRUE))
aFit

aFitKnn <- train(label ~ ., data = a, method = "knn", preProc = c("center", "scale"))
aFitKnn

# Try using the model to predict test data labels
testData <- read.csv("/Users/tom/projects-workspace/set-game/data/test-out-shading.csv", col.names = c("label", "v1"))
testData$label <- as.factor(testData$label)
testDataNoLabel <- testData[-c(1)]

predictions <- predict(aFitKnn, newdata = testDataNoLabel)
predictions

print(postResample(pred=predictions, obs=as.factor(testData$label)))

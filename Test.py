import csv
import random
import tensorflow as tf
import numpy as np
import os

def extract_data(filename):
    with open(filename) as csvfile:
        readCSV = csv.reader(csvfile, delimiter=" ")

        player = []
        X_board = []
        O_board = []
        moves = []
        score = []

        for row in readCSV:
            player.append(int(row[0]))
            X_board.append([int(x) for x in row[1:43]])
            O_board.append([int(x) for x in row[43:85]])
            moves.append([int(x) for x in row[85:92]])
            score.append(int(row[92]))

        return [player, X_board, O_board, moves, score]

def next(num, data):
    id = random.sample(population=range(0, len(data[0])), k=num)
    # id = [0]
    player_num = []
    X_board_batch = []
    O_board_batch = []
    moves_batch = []
    score_batch = []
    for n in id:
        player_num.append(data[0][n])
        X_board_batch.append(data[1][n])
        O_board_batch.append(data[2][n])
        moves_batch.append(data[3][n])
        score_batch.append(data[4][n])

    player_batch = []
    for x in player_num:
        if x == 1:
            player_batch.append([1]*42)
        else:
            player_batch.append([0]*42)

    input_batch = []
    for n in range(num):
        input = []
        input = player_batch[n] + X_board_batch[n] + O_board_batch[n]
        input_batch.append(input)

    input_batch_matrix = np.matrix(input_batch).astype(dtype=np.float32)
    moves_batch_matrix = np.matrix(moves_batch).astype(dtype=np.float32)
    score_batch_matrix = np.matrix(score_batch).astype(dtype=np.float32).swapaxes(0, 1)

    return [input_batch_matrix, moves_batch_matrix, score_batch_matrix]

def weight_variable(shape):
    #Xavier initialization
    stddev = np.sqrt(2.0 / (sum(shape)))
    initial = tf.truncated_normal(shape, stddev=stddev)
    return tf.Variable(initial)

#Bias for weights not followed by BatchNorm (needs to be trained)
def bias_variable(shape):
    initial = tf.constant(0.0, shape=shape)
    return tf.Variable(initial)

#Bias for weights followed by BatchNorm (not trained)
def bn_bias_variable(shape):
    initial = tf.constant(0.0, shape=shape)
    return tf.Variable(initial, trainable=False)

def conv2d(x, W):
    return tf.nn.conv2d(x, W, data_format='NCHW', strides=[1, 1, 1, 1], padding='SAME')

class TFProcess:
    def __init__(self):
        self.session = tf.Session()

        #For exporting
        self.weights = []

        #TF variables
        # self.next_batch = next_batch
        self.next_batch = None
        self.global_step = tf.Variable(0, name='global_step', trainable=False)
        # self.x = next_batch[0] # tf.placeholder(tf.float32, [None, 3, 7*6])
        # self.y_ = next_batch[1] # tf.placeholder(tf.float32, [None, 7])
        # self.z_ = next_batch[2] # # tf.placeholder(tf.float32, [None, 1])
        self.x = tf.placeholder(tf.float32, [None, 126])
        self.y_ = tf.placeholder(tf.float32, [None, 7])
        self.z_ = tf.placeholder(tf.float32, [None, 1])
        self.training = tf.placeholder(tf.bool)
        self.batch_norm_count = 0
        self.y_conv, self.z_conv = self.construct_net(self.x)

        # Calculate loss on policy head
        cross_entropy = \
            tf.nn.softmax_cross_entropy_with_logits(labels=self.y_,
                                                    logits=self.y_conv)
        self.policy_loss = tf.reduce_mean(cross_entropy)

        # Loss on value head
        self.mse_loss = \
            tf.reduce_mean(tf.squared_difference(self.z_, self.z_conv))

        #Regularizer
        regularizer = tf.contrib.layers.l2_regularizer(scale=0.0001)
        reg_variables = tf.trainable_variables()
        self.reg_term = tf.contrib.layers.apply_regularization(regularizer, reg_variables)

        loss = 1.0 * self.policy_loss + 1.0 * self.mse_loss + self.reg_term

        opt_op = tf.train.MomentumOptimizer(learning_rate=0.00005, momentum=0.9, use_nesterov=True)

        self.update_ops = tf.get_collection(tf.GraphKeys.UPDATE_OPS)
        with tf.control_dependencies(self.update_ops):
            self.train_op = opt_op.minimize(loss, global_step=self.global_step)

        correct_prediction = tf.equal(tf.argmax(self.y_conv, 1), tf.argmax(self.y_, 1))
        correct_prediction = tf.cast(correct_prediction, tf.float32)
        self.accuracy = tf.reduce_mean(correct_prediction)

        self.avg_policy_loss = None
        self.avg_mse_loss = None
        self.avg_reg_term = None
        self.time_start = None

        self.init = tf.global_variables_initializer()
        self.saver = tf.train.Saver()

        self.session.run(self.init)

    def restore(self, extension):
        file = os.path.join(os.getcwd(), extension)
        print("Restoring from {0}".format(file))
        print(file)
        self.saver.restore(self.session, file)

    def train(self, iterations, batch_size, data_train, data_test):
        for i in range(iterations):
            next_batch = next(batch_size, data_train)

            # Run training for this batch
            policy_loss, mse_loss, reg_term, _ = self.session.run(
                [self.policy_loss, self.mse_loss, self.reg_term, self.train_op],
                feed_dict={self.x:next_batch[0], self.y_:next_batch[1], self.z_:next_batch[2], self.training: True})
            steps = tf.train.global_step(self.session, self.global_step)

            # Keep running averages
            decay = 0.999
            if self.avg_policy_loss:
                self.avg_policy_loss = decay * self.avg_policy_loss + (1 - decay) * policy_loss
            else:
                self.avg_policy_loss = policy_loss
            if self.avg_mse_loss:
                self.avg_mse_loss = decay * self.avg_mse_loss + (1 - decay) * mse_loss
            else:
                self.avg_mse_loss = mse_loss
            if self.avg_reg_term:
                self.avg_reg_term = decay * self.avg_reg_term + (1 - decay) * reg_term
            else:
                self.avg_reg_term = reg_term

            if steps % 100 == 0:
                print("step {}, policy loss={:g} mse={:g} reg={:g}".format(steps, self.avg_policy_loss, self.avg_mse_loss, self.avg_reg_term))

            if steps % 1000 == 0:
                sum_accuracy = 0
                sum_mse = 0
                test_iterations = 100
                for _ in range(0, test_iterations):
                    next_batch = next(batch_size, data_test)
                    train_accuracy, train_mse = self.session.run(
                        [self.accuracy, self.mse_loss],
                        feed_dict={self.x:next_batch[0], self.y_:next_batch[1], self.z_:next_batch[2], self.training: False})
                    sum_accuracy += train_accuracy
                    sum_mse += train_mse
                sum_accuracy /= test_iterations
                sum_mse /= test_iterations
                print("step {}, training accuracy={:g}%, mse={:g}".format(
                    steps, sum_accuracy * 100.0, sum_mse))
                path = os.path.join(os.getcwd(), "model")
                save_path = self.saver.save(self.session, path, global_step=steps)
                print("Model saved in file: {}".format(save_path))

    def construct_net(self, planes):
        #Network Structure
        RESIDUAL_FILTERS = 128
        RESIDUAL_BLOCKS = 19

        # NCHW format
        # batch, 3 channels of 6 high 7 long
        x_planes = tf.reshape(planes, [-1, 3, 6, 7])

        #Input convolution
        flow = self.conv_block(x_planes, filter_size=3, input_channels=3, output_channels=RESIDUAL_FILTERS)

        #Residual tower
        for _ in range(0, RESIDUAL_BLOCKS):
            flow = self.residual_block(flow, RESIDUAL_FILTERS)

        #Policy head
        conv_pol = self.conv_block(flow, filter_size=1, input_channels=RESIDUAL_FILTERS, output_channels=2)
        h_conv_pol_flat = tf.reshape(conv_pol, [-1, 2*6*7])
        W_fc1 = weight_variable([2*6*7, 7])
        b_fc1 = bias_variable([7])
        self.weights.append(W_fc1)
        self.weights.append(b_fc1)
        h_fc1 = tf.add(tf.matmul(h_conv_pol_flat, W_fc1), b_fc1)

        #Value head
        conv_val = self.conv_block(flow, filter_size=1, input_channels=RESIDUAL_FILTERS, output_channels=1)
        h_conv_val_flat = tf.reshape(conv_val, [-1, 6*7])
        W_fc2 = weight_variable([6*7, 256]) # Is 256 necessary for a small problem like connect 4? We have 42 neurons mapping to 256 here
        b_fc2 = bias_variable([256])
        self.weights.append(W_fc2)
        self.weights.append(b_fc2)
        h_fc2 = tf.nn.relu(tf.add(tf.matmul(h_conv_val_flat, W_fc2), b_fc2))

        W_fc3 = weight_variable([256, 1])
        b_fc3 = bias_variable([1])
        self.weights.append(W_fc3)
        self.weights.append(b_fc3)
        h_fc3 = tf.nn.tanh(tf.add(tf.matmul(h_fc2, W_fc3), b_fc3))

        return h_fc1, h_fc3

    def get_batchnorm_key(self):
        result = "bn" + str(self.batch_norm_count)
        self.batch_norm_count += 1
        return result

    def conv_block(self, inputs, filter_size, input_channels, output_channels):
        #Convolve with kernal size 3x3 stride 1
        #Batch normalization
        #Relu

        W_conv = weight_variable([filter_size, filter_size, input_channels, output_channels])
        b_conv = bn_bias_variable([output_channels])

        #Apply a unique scope that we can store, and use to look them back up later on
        weight_key = self.get_batchnorm_key()
        self.weights.append(weight_key + "/batch_normalization/moving_mean:0")
        self.weights.append(weight_key + "/batch_normalization/moving_variance:0")

        with tf.variable_scope(weight_key):
            h_bn = \
                tf.layers.batch_normalization(
                    conv2d(inputs, W_conv),
                    epsilon=1e-5, axis=1, fused=True,
                    center=False, scale=False,
                    training=self.training)
        h_conv = tf.nn.relu(h_bn)
        return h_conv

    def residual_block(self, inputs, channels):
        #First convnet
        orig = tf.identity(inputs)
        W_conv_1 = weight_variable([3, 3, channels, channels])
        b_conv_1 = bn_bias_variable([channels])
        self.weights.append(W_conv_1)
        self.weights.append(b_conv_1)
        weight_key_1 = self.get_batchnorm_key()
        self.weights.append(weight_key_1 + "/batch_normalization/moving_mean:0")
        self.weights.append(weight_key_1 + "/batch_normalization/moving_variance:0")

        # Second convnet
        W_conv_2 = weight_variable([3, 3, channels, channels])
        b_conv_2 = bn_bias_variable([channels])
        self.weights.append(W_conv_2)
        self.weights.append(b_conv_2)
        weight_key_2 = self.get_batchnorm_key()
        self.weights.append(weight_key_2 + "/batch_normalization/moving_mean:0")
        self.weights.append(weight_key_2 + "/batch_normalization/moving_variance:0")

        with tf.variable_scope(weight_key_1):
            h_bn1 = \
                tf.layers.batch_normalization(
                    conv2d(inputs, W_conv_1),
                    epsilon=1e-5, axis=1, fused=True,
                    center=False, scale=False,
                    training=self.training)
        h_out_1 = tf.nn.relu(h_bn1)
        with tf.variable_scope(weight_key_2):
            h_bn2 = \
                tf.layers.batch_normalization(
                    conv2d(h_out_1, W_conv_2),
                    epsilon=1e-5, axis=1, fused=True,
                    center=False, scale=False,
                    training=self.training)
        h_out_2 = tf.nn.relu(tf.add(h_bn2, orig))
        return h_out_2

def main():
    process = TFProcess()
    process.restore("model-277000")

    print("Reading data")
    data_train = extract_data('train_data.txt')
    data_test = extract_data('test_data.txt')
    print("Finished reading data")

    process.train(10000000000, 256, data_train, data_test)

main()
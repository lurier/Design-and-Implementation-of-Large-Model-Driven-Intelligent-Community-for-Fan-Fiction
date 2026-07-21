-- ================================================
-- FanFaction Test Data Restoration Script
-- Restores: Articles, Comments, Interactions, Reading History, etc.
-- ================================================

USE `fanfaction`;

-- ================================================
-- 1. Insert Test Articles (20 articles)
-- ================================================
INSERT INTO `article` (`title`, `content`, `summary`, `tags`, `cover_image`, `author_id`, `view_count`, `like_count`, `favorite_count`, `comment_count`, `status`) VALUES
('Chapter 1: First Meeting', 'It was a sunny afternoon. I met her in the corner of the library. Her long hair fell like a waterfall, and she was reading a book intently...', 'The protagonist meets someone special in the library, beginning a beautiful story.', 'Romance, School, First Love', 'https://picsum.photos/seed/story1/800/600', 3, 1523, 89, 34, 12, 1),
('Chapter 2: Promise', 'The weather was exceptionally good today. We met at the coffee shop near the school gate. She wore a white dress...', 'The first date between two people, full of sweetness and nervousness.', 'Romance, School, Date', 'https://picsum.photos/seed/story2/800/600', 3, 1245, 76, 28, 8, 1),
('Chapter 3: Parting', 'Graduation season is always full of sorrow. Standing on the train station platform, we looked at each other in silence...', 'A touching graduation parting scene.', 'Romance, Youth, Parting', 'https://picsum.photos/seed/story3/800/600', 3, 2134, 156, 67, 23, 1),
('Doujin: Naruto - New Mission', 'After Naruto became the Seventh Hokage, the Hidden Leaf Village entered an era of peace. However, new threats are quietly approaching...', 'Naruto doujin work, continuing the classic story.', 'Naruto, Doujin, Adventure', 'https://picsum.photos/seed/naruto/800/600', 3, 3421, 234, 89, 45, 1),
('Doujin: One Piece - New World Adventure', 'Luffy and his companions continue to sail in the New World, searching for the legendary One Piece...', 'One Piece doujin, passionate adventure.', 'One Piece, Doujin, Passion', 'https://picsum.photos/seed/onepiece/800/600', 3, 4532, 312, 123, 67, 1),
('Book Review: To Live', 'Yu Hua\'s "To Live" is a thought-provoking work. The protagonist Fugui\'s life is full of hardships...', 'A deep book review of the classic literary work "To Live".', 'Book Review, Literature, Reflection', 'https://picsum.photos/seed/book1/800/600', 3, 876, 45, 23, 8, 1),
('Recommendation: 10 Must-Read Romance Novels', 'As a seasoned book fan, today I recommend ten of the best romance novels in my mind...', 'Romance novel recommendation book list.', 'Recommendation, Romance, Book List', 'https://picsum.photos/seed/recommend/800/600', 3, 1567, 98, 56, 15, 1),
('Chapter 4: Reunion', 'Five years later, we met again in the same city. He became more mature and steady...', 'A story about reunion after a long separation.', 'Romance, Urban, Reunion', 'https://picsum.photos/seed/story4/800/600', 3, 1876, 134, 45, 19, 1),
('Doujin: Attack on Titan - Another Ending', 'What if Eren didn\'t choose that path? What would the world become? Let\'s look at the story in this parallel world...', 'Attack on Titan doujin, alternate ending.', 'AOT, Doujin, Parallel World', 'https://picsum.photos/seed/aot/800/600', 3, 2987, 187, 76, 34, 1),
('Chapter 5: Confession', 'Finally, in the season when cherry blossoms bloomed, he gathered courage to confess to her...', 'A classic confession scene, full of girlish heart.', 'Romance, School, Confession', 'https://picsum.photos/seed/story5/800/600', 3, 2345, 178, 89, 28, 1),
('Analysis: Development Trends of Online Literature', 'In recent years, online literature has shown vigorous development...', 'Online literature industry analysis article.', 'Analysis, Literature, Industry', 'https://picsum.photos/seed/analysis/800/600', 3, 654, 34, 12, 5, 1),
('Chapter 6: Misunderstanding', 'A small misunderstanding caused the relationship between two people to fall into a cold war...', 'Misunderstandings and contradictions in relationships.', 'Romance, Angst, Misunderstanding', 'https://picsum.photos/seed/story6/800/600', 3, 1987, 145, 67, 21, 1),
('Doujin: Demon Slayer - Hashira Training', 'Tanjiro participated in the Hashira\'s special training. Each Hashira has their own unique training methods...', 'Demon Slayer doujin, passionate growth.', 'Demon Slayer, Doujin, Training', 'https://picsum.photos/seed/demonslayer/800/600', 3, 3654, 267, 98, 52, 1),
('Writing Tips: How to Write Good Doujin Novels', 'As a doujin author, I am often asked how to write good doujin novels...', 'Sharing writing skills for doujin novels.', 'Writing, Doujin, Skills', 'https://picsum.photos/seed/writing/800/600', 3, 789, 56, 34, 9, 1),
('Chapter 7: Reconciliation', 'After some thought, she finally decided to forgive him. Two people in love should not miss each other...', 'Misunderstanding cleared, reconciled.', 'Romance, Healing, Reconciliation', 'https://picsum.photos/seed/story7/800/600', 3, 2123, 167, 78, 25, 1),
('Chapter 8: Future', 'Standing at the university gate, they held hands, looking forward to a beautiful future...', 'A beautiful ending full of hope.', 'Romance, School, Ending', 'https://picsum.photos/seed/story8/800/600', 3, 2876, 234, 112, 38, 1),
('List: Those Anime Scenes That Make People Cry', 'There are too many touching scenes in anime. Today let\'s list those classic tear-jerking moments...', 'Anime scene list, deeply moving.', 'Anime, List, Touching', 'https://picsum.photos/seed/anime/800/600', 3, 1234, 87, 45, 12, 1),
('Doujin: Jujutsu Kaisen - New Cursed Spirit', 'Yuji Itadori faced the newly appeared special grade cursed spirit, showing amazing growth...', 'Jujutsu Kaisen doujin, intense battle.', 'JJK, Doujin, Battle', 'https://picsum.photos/seed/jjk/800/600', 3, 4123, 298, 134, 71, 1),
('Chapter 9: Proposal', 'On that romantic night, he knelt down on one knee, took out the prepared ring, and looked at her deeply...', 'A romantic proposal scene.', 'Romance, Proposal, Romantic', 'https://picsum.photos/seed/story9/800/600', 3, 3456, 289, 145, 56, 1),
('Chapter 10: Wedding', 'Finally this day came. The church was full of guests, she wore a white wedding dress, walking slowly towards him...', 'A perfect wedding ending.', 'Romance, Wedding, Happiness', 'https://picsum.photos/seed/story10/800/600', 3, 4567, 378, 189, 78, 1);

-- ================================================
-- 2. Insert Test Comments (50 comments)
-- ================================================
INSERT INTO `comment` (`user_id`, `article_id`, `parent_id`, `content`, `like_count`, `status`) VALUES
-- Comments for article 1
(2, 1, 0, 'Written so well! The library meeting scene reminds me of my first love.', 12, 1),
(2, 1, 0, 'Looking forward to the follow-up, author go for it!', 8, 1),
(2, 1, 0, 'The writing is very delicate, strong visual sense.', 5, 1),
-- Comments for article 2
(2, 2, 0, 'The coffee shop date description is so sweet!', 15, 1),
(2, 2, 0, 'Reading this makes me want to go on a date~', 7, 1),
-- Comments for article 3
(2, 3, 0, 'The graduation parting made me cry, so touching.', 23, 1),
(2, 3, 0, 'Hope they can meet again in the future.', 9, 1),
(2, 3, 0, 'Author please don\'t be too angsty!', 11, 1),
-- Comments for article 4 (Naruto)
(2, 4, 0, 'Naruto doujin is well written, the story after Naruto became Hokage is very attractive.', 18, 1),
(2, 4, 0, 'Looking forward to new adventures, what new threats will the Hidden Leaf Village face?', 14, 1),
(2, 4, 0, 'As a Naruto fan, this doujin must be supported!', 21, 1),
-- Comments for article 5 (One Piece)
(2, 5, 0, 'One Piece doujin, Luffy will always be our king!', 25, 1),
(2, 5, 0, 'New World adventure, sounds very exciting.', 16, 1),
-- Comments for article 6 (Book review)
(2, 6, 0, '"To Live" is indeed a work worth thinking about.', 8, 1),
(2, 6, 0, 'Yu Hua\'s writing is very powerful, thank you for sharing.', 6, 1),
-- Comments for article 7 (Recommendations)
(2, 7, 0, 'Thanks for the recommendation, just in book drought, going to read now!', 10, 1),
(2, 7, 0, '"He Yi Sheng Xiao Mo" is indeed a classic, I also recommend it!', 13, 1),
-- Comments for article 8 (Reunion)
(2, 8, 0, 'I really like the setting of reunion after long separation.', 9, 1),
(2, 8, 0, 'Meeting after five years, what story will happen?', 7, 1),
-- Comments for article 9 (AOT)
(2, 9, 0, 'Attack on Titan alternate ending, very creative.', 19, 1),
(2, 9, 0, 'If Eren chose another path, what would the world be like? Looking forward to it!', 15, 1),
-- Comments for article 10 (Confession)
(2, 10, 0, 'The confession scene is described so well, full of girlish heart!', 17, 1),
(2, 10, 0, 'Confessing in the season when cherry blossoms bloom, so romantic.', 12, 1),
-- Reply comments
(2, 1, 34, 'Thanks for support! Will continue to update.', 3, 1),
(2, 3, 41, 'Don\'t worry, there will be a turning point later.', 5, 1),
(2, 4, 45, 'Naruto fans shake hands! Looking forward to the follow-up together.', 8, 1),
-- More comments
(2, 11, 0, 'The analysis of online literature is very good, learned a lot.', 6, 1),
(2, 12, 0, 'Misunderstandings are the most angsty, author please be gentle.', 14, 1),
(2, 13, 0, 'Demon Slayer doujin is also very good, Tanjiro go for it!', 20, 1),
(2, 14, 0, 'Writing skills sharing is very practical, thank you author.', 9, 1),
(2, 15, 0, 'Reconciled, great. Two people in love should not miss each other.', 11, 1),
(2, 16, 0, 'Beautiful ending, bless them!', 16, 1),
(2, 17, 0, 'Anime list is very interesting, cried a lot from many scenes.', 13, 1),
(2, 18, 0, 'Jujutsu Kaisen doujin, Yuji Itadori is so cool!', 22, 1),
(2, 19, 0, 'Proposal scene is so romantic, envious.', 18, 1),
(2, 20, 0, 'Perfect ending, thank you author for the companionship.', 24, 1);

-- ================================================
-- 3. Insert Test Interactions (Likes and Favorites)
-- ================================================
-- User 2 likes articles
INSERT INTO `interaction` (`user_id`, `article_id`, `type`) VALUES
(2, 1, 1), (2, 2, 1), (2, 3, 1), (2, 4, 1), (2, 5, 1),
(2, 6, 1), (2, 7, 1), (2, 8, 1), (2, 9, 1), (2, 10, 1),
(2, 11, 1), (2, 12, 1), (2, 13, 1), (2, 14, 1), (2, 15, 1),
(2, 16, 1), (2, 17, 1), (2, 18, 1), (2, 19, 1), (2, 20, 1);

-- User 2 favorites some articles
INSERT INTO `interaction` (`user_id`, `article_id`, `type`) VALUES
(2, 1, 2), (2, 3, 2), (2, 4, 2), (2, 5, 2), (2, 10, 2),
(2, 13, 2), (2, 16, 2), (2, 19, 2), (2, 20, 2);

-- ================================================
-- 4. Insert Test Reading History
-- ================================================
INSERT INTO `reading_history` (`user_id`, `article_id`, `read_duration`) VALUES
(2, 1, 300), (2, 2, 240), (2, 3, 360), (2, 4, 420), (2, 5, 380),
(2, 6, 180), (2, 7, 200), (2, 8, 290), (2, 9, 350), (2, 10, 310),
(2, 11, 250), (2, 12, 280), (2, 13, 400), (2, 14, 220), (2, 15, 270),
(2, 16, 320), (2, 17, 190), (2, 18, 370), (2, 19, 340), (2, 20, 410);

-- ================================================
-- 5. Update User Data
-- ================================================
-- Update user 2's nickname
UPDATE `sys_user` SET `nickname` = 'Book Fan Xiao Wang', `avatar` = 'https://picsum.photos/seed/user2/200/200' WHERE `username` = 'test_user';

-- Update user 3's nickname
UPDATE `sys_user` SET `nickname` = 'Famous Writer', `avatar` = 'https://picsum.photos/seed/user3/200/200' WHERE `username` = 'creator';

-- ================================================
-- Completion Message
-- ================================================
SELECT 'Test data restoration completed!' AS message;
SELECT CONCAT('Articles: ', COUNT(*)) AS article_count FROM article;
SELECT CONCAT('Comments: ', COUNT(*)) AS comment_count FROM comment;
SELECT CONCAT('Interactions: ', COUNT(*)) AS interaction_count FROM interaction;
SELECT CONCAT('Reading history: ', COUNT(*)) AS history_count FROM reading_history;

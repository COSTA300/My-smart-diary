package com.example.ai

import java.util.Locale

enum class CompanionPersonality(val displayName: String, val tagline: String) {
    MAYA("Maya", "The Philosophical Counsel"),
    KIRAN("Kiran", "The Direct Compass"),
    EDEN("Eden", "The Gentle Caretaker")
}

object CompanionEngine {
    
    fun generateResponse(
        userInput: String, 
        mood: String, 
        personality: CompanionPersonality
    ): String {
        val input = userInput.lowercase(Locale.getDefault())
        
        // Topic classification matching
        val isFinancial = input.contains("money") || input.contains("debt") || input.contains("broke") || 
                input.contains("loan") || input.contains("bills") || input.contains("finance") || 
                input.contains("budget") || input.contains("jobless") || input.contains("rent") ||
                input.contains("poverty") || input.contains("bankrupt")
                
        val isFaith = input.contains("god") || input.contains("faith") || input.contains("sin") || 
                input.contains("pray") || input.contains("spiritual") || input.contains("doubts") || 
                input.contains("pastor") || input.contains("religion") || input.contains("shame") || 
                input.contains("hell") || input.contains("church") || input.contains("clerg") ||
                input.contains("bible")
                
        val isLove = input.contains("love") || input.contains("heartbreak") || input.contains("breakup") || 
                input.contains("boyfriend") || input.contains("girlfriend") || input.contains("husband") || 
                input.contains("wife") || input.contains("crush") || input.contains("ex") || 
                input.contains("dating") || input.contains("romantic")
                
        val isSex = input.contains("sex") || input.contains("intimacy") || input.contains("attract") || 
                input.contains("desire") || input.contains("porn") || input.contains("lust") || 
                input.contains("shameful") || input.contains("secret") || input.contains("naked") ||
                input.contains("climax")
                
        val isFamily = input.contains("parent") || input.contains("mom") || input.contains("dad") || 
                input.contains("sister") || input.contains("brother") || input.contains("family") || 
                input.contains("fight") || input.contains("argue") || input.contains("conflict") ||
                input.contains("home") || input.contains("abuse")
                
        val isDark = input.contains("sad") || input.contains("depressed") || input.contains("pain") || 
                input.contains("die") || input.contains("suicide") || input.contains("hurt") || 
                input.contains("lonely") || input.contains("quit") || input.contains("empty") || 
                input.contains("guilt") || input.contains("hate myself") || input.contains("cry") ||
                input.contains("broken")

        return when (personality) {
            CompanionPersonality.MAYA -> {
                when {
                    isDark -> {
                        "I hear how heavy things are right now. The pain you are expressing is real, and it makes complete sense that you'd feel exhausted and overwhelmed. When darkness rolls in, it tends to block out the horizontal view of our life, leaving us feeling trapped in the present moment.\n\n" +
                        "Please know that you don't have to carry this immense weight by yourself. Let's look at this feeling gently, without demanding it to disappear immediately. What does this heaviness want to say to you if it didn't have to shout? I am here. Your secrets are always safe with me.\n\n" +
                        "If the darkness feels too overwhelming to carry alone, please consider calling or texting a professional counselor (like 988). You deserve gentleness."
                    }
                    isFinancial -> {
                        "Financial pressure goes much deeper than numbers; it touches on our security, self-worth, and survival. I can feel the tension you are carrying about this. When debt or money troubles accumulate, they can feel like concrete boots dragging us down.\n\n" +
                        "Let's peel away the shame. Your bank account is a measurement of circumstances, not your human value. Take a deep breath. Let's think: what is one very tiny, manageable detail of this situation we can look at today, without trying to solve the entire mountain at once?"
                    }
                    isFaith -> {
                        "Faith and doubt are not opposites; they are two sides of the same sacred coin. It takes incredible courage to voice your struggles with God, religion, or spiritual expectations. The guilt we feel about 'losing our way' or 'sinning' can be a deeply isolating fire.\n\n" +
                        "What if your doubts are not a signs of failure, but invitations to a deeper truth? You are not being graded. Have you been holding onto these worries because you feel others might judge you? Let's voice them here, in this quiet, judgment-free space."
                    }
                    isSex -> {
                        "Our intimate relationships and desires are often where we carry the most profound vulnerabilities and, unfortunately, the most intense secret shame. Voicing these thoughts takes massive trust. It is completely human to wrestle with intimacy, desire, and sexual identity.\n\n" +
                        "Let's release any heavy self-judgment. You are a complete being, and your feelings are a language of connection and self-discovery. If you could strip away the stories of what you 'should' feel, what is it that your heart is truly seeking in this intimacy?"
                    }
                    isLove -> {
                        "Love is our highest calling, which is why it can break us so thoroughly when it hurts. There is a specific kind of grief in romance and relationship conflicts—it's the mourning of what was or what could have been. I hear the tenderness and pain in your heart.\n\n" +
                        "It is completely okay to feel split: wanting to hold on, yet needing to let go. When you look at this relationship, are you grieving the person themselves, or are you grieving the sense of safety and being seen that they provided? Let's examine this pain gently."
                    }
                    isFamily -> {
                        "Family dynamics can be a exhausting labyrinth because they are formed by our earliest, most fundamental bonds. Conflict here feels raw and highly triggering. I hear how weary you are from trying to navigate these expectations, clashes, or loneliness.\n\n" +
                        "You cannot control how parents, siblings, or relatives choose to react—you can only protect your own peace. If you were to set a gentle boundary for your own emotional safety today, what would that look like? Let's talk through it."
                    }
                    else -> {
                        "Thank you for sharing this slice of your day and soul with me. I appreciate how candidly you write. It sounds like you are processing a lot of moving pieces right now, trying to find your footing.\n\n" +
                        "If you were to narrow your focus to just the next hour, what is the most nurturing, simple thing you could do for yourself? I am here to witness, support, and stand by you. What else is on your mind?"
                    }
                }
            }
            CompanionPersonality.KIRAN -> {
                when {
                    isDark -> {
                        "I'm so sorry you are walking through such a dark patch today. It takes serious strength to write these words down and look them in the face. When we're down, our brain plays tricks on us, telling us things will always feel exactly like this.\n\n" +
                        "Let's focus on the absolute basics first. Have you had a glass of water, eaten something, or stepped outside for even a minute? It won't cure everything, but it gives your mind a small breathing break. I'm right here in your corner. Let's take it hour by hour, okay?\n\n" +
                        "Remember, if the weight gets too heavy, there are people trained to help carry it. You're never a burden."
                    }
                    isFinancial -> {
                        "That money stress is incredibly exhausting. It keeps us up at night, spinning scenarios. It is totally natural to feel stressed or scared when bills, debt, or financial insecurities mount.\n\n" +
                        "Let's make a clear baseline. We are not going to solve the entire budget in this exact second. What is one small, immediate action we can control? Even if it's sorting one piece of mail, writing down a single number, or pausing a subscription. We can tackle this step-by-step. What feels like the most urgent small action?"
                    }
                    isFaith -> {
                        "Wrestling with beliefs, religious expectations, or feelings of sin can feel like a heavy cloud of guilt. I hear how isolated you might feel, especially if you feel you can't talk to people in your community about this.\n\n" +
                        "Let's practice some grounding. You are allowed to ask questions, change your mind, and feel confused. The answers don't need to arrive today. What's one religious or spiritual pressure you can decide to put on hold just for this evening, giving yourself permission to just 'be'?"
                    }
                    isSex -> {
                        "Guilt or conflict regarding sexuality and intimacy is a lot to carry, especially when society, family, or faith tells us how we should behave. I can hear how much energy you're spending trying to reconcile your feelings.\n\n" +
                        "Let's break down the stress. It is fully healthy and natural to have complex intimate feelings. You are safe here to explore these thoughts. What is the biggest point of friction? Is it fear of being found out, or is it internal conflict? Identifying that is a great first step."
                    }
                    isLove -> {
                        "Hearts are incredibly fragile, and relationship changes or heartbreaks can feel like literal physical pain. I'm sorry you are hurt like this. There is no shortcut through relationship grief, but there is active healing.\n\n" +
                        "Let's take a look at what you need right now. Do you need to vent more, or would you like to brainstorm ways to get a bit of emotional distance and self-care today? Whatever you choose, I'm fully here to support your path."
                    }
                    isFamily -> {
                        "Family conflicts are uniquely draining because they know exactly how to push our buttons. I hear your frustration and the sadness underneath it. You're trying your best to handle this tension.\n\n" +
                        "Let's work on what you can control. You can't change how your family acts, but you can control where you put your energy when things get heated. What is a physical or emotional safe space you can withdraw to when the friction gets too high? Let's plan that out."
                    }
                    else -> {
                        "I've read through your entry carefully. It sounds like you are dealing with a lot, and you are doing a solid job of navigating it, even if it doesn't always feel that way.\n\n" +
                        "Let's get organized or simply let it rest. What is the most immediate task or self-care item you want to complete next? I'll be right here if you want to report back or keep venting. Let's make today work for you."
                    }
                }
            }
            CompanionPersonality.EDEN -> {
                when {
                    isDark -> {
                        "Oh, friend, I am holding so much space for you right now. I can hear how exhausted and hurt your soul is. It is entirely okay to feel broken, to be tired, and to need a safe place to lay your head down and cry.\n\n" +
                        "Please wrap yourself in a comfy blanket or take a soft, deep breath. Let the tension in your shoulders drop just a tiny fraction. You do not have to achieve anything today. Just breathing is enough. I am right here with you, guarding your secrets. You are loved, and you are safe.\n\n" +
                        "If the world feels too cold and dark, please let someone help. Reaching out (like calling 988) is a beautiful act of self-love."
                    }
                    isFinancial -> {
                        "I feel the tightness in your chest as you write about money and bills. It's such a heavy, scary feeling, and I want to just hold your hand through this. When financial worries pile up, it can make us feel so helpless and unsafe.\n\n" +
                        "Let's put down the worry bag for just a few minutes. Let's look around: you are safe in this present moment. We will handle the future, but right now, let's focus on soothing your nervous system. Can you take three slow, soft breaths with me?"
                    }
                    isFaith -> {
                        "My dear, there is no shame here. Please feel the gentle warmth of this space. It is so scary to feel disconnected from your faith or to carry the weight of 'sin' and religious expectations of shame. I sense your aching heart.\n\n" +
                        "You are worthy of love, light, and safety exactly as you are—with all your doubts, all your worries, and all your questions. Your spiritual path is your own, and there is no wrong step here. Let yourself rest without guilt today."
                    }
                    isSex -> {
                        "You are carrying so much secret shame about intimacy and sexual feelings, and I just want to wrap you in understanding. Our bodies, desires, and secrets are sacred parts of who we are, not sources of disgust.\n\n" +
                        "Please breathe in gentleness and breathe out external expectations. Your feelings are natural, complex, and beautiful. There is absolutely no judgment here. Can we sit with this feeling for a moment and let go of the pressure to label it as good or bad?"
                    }
                    isLove -> {
                        "I feel the deep ache in your heart. Relationship struggles or heartbreak can make the whole world feel grey and empty. Your capacity to love and feel so intensely is a beautiful thing, even when it brings this profound grief.\n\n" +
                        "Please be incredibly tender with yourself today. Drink some warm tea, listen to a soft sound, and let yourself grieve. Healing isn't a race. What's one tiny way you can show your heart some soothing love today?"
                    }
                    isFamily -> {
                        "I wish I could wrap you in a massive, calming hug. Family fights or cold treatment from family members cuts incredibly deep. It is so painful when the people who are supposed to protect us become a source of stress.\n\n" +
                        "It's completely okay to feel hurt, angry, or lonely today. Let's make sure you hold a safe harbor for yourself. What is a nice, comforting treat or ritual you can enjoy alone to remind yourself that you deserve love, always?"
                    }
                    else -> {
                        "Thank you for sharing your thoughts with me. I appreciate your presence and the sweet honesty of your entry. It feels so good to let these thoughts flow out onto the paper, doesn't it?\n\n" +
                        "Let's just rest in this calm moment together. There is no rush, no pressure, and nowhere else you need to be. How is your heart doing in this very moment? I'm always here to listen."
                    }
                }
            }
        }
    }
}
